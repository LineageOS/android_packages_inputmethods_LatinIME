//
// Created by msj on 2017/3/9.
//

#ifndef GOOGLEKEYBOARDV7_GESTURE_WEIGHTING_H
#define GOOGLEKEYBOARDV7_GESTURE_WEIGHTING_H



#include "defines.h"
#include "suggest/core/dicnode/dic_node_utils.h"
#include "suggest/core/dictionary/error_type_utils.h"
#include "suggest/core/layout/touch_position_correction_utils.h"
#include "suggest/core/layout/proximity_info.h"
#include "suggest/core/policy/weighting.h"
#include "suggest/core/session/dic_traverse_session.h"
#include "suggest/policyimpl/typing/scoring_params.h"
#include "suggest/policyimpl/gesture/scoring_params_g.h"
#include "utils/char_utils.h"

namespace latinime {

    class DicNode;
    struct DicNode_InputStateG;
    class MultiBigramMap;

    class GestureWeighting : public Weighting {
    public:
        static const GestureWeighting *getInstance() { return &sInstance; }

    protected:
        float getTerminalSpatialCost(const DicTraverseSession *const traverseSession,
                                     const DicNode *const dicNode) const {
            const int point0index = dicNode->getInputIndex(0);
            const int tmpInputSize = traverseSession->getInputSize();
            float cost = 0;
            for (int i = point0index; i < tmpInputSize; i++) {
                cost += traverseSession->getProximityInfoState(0)->getProbability(i, NOT_AN_INDEX);
            }
            if (point0index > tmpInputSize) {
                cost += (point0index - tmpInputSize) * ScoringParamsG::DISTANCE_WEIGHT_EXCEEDING_INPUT_SIZE;
            }

            return cost;
        }

        float getTerminalLanguageCost(const DicTraverseSession *const traverseSession,
                                      const DicNode *const dicNode, const float dicNodeLanguageImprobability) const {
            return dicNodeLanguageImprobability * ScoringParamsG::DISTANCE_WEIGHT_LANGUAGE;
        }

        float getCompletionCost(const DicTraverseSession *const traverseSession,
                                const DicNode *const dicNode) const {
            // The auto completion starts when the input index is same as the input size
            const bool firstCompletion = dicNode->getInputIndex(0)
                                         == traverseSession->getInputSize();
            // TODO: Change the cost for the first completion for the gesture?
            const float cost = firstCompletion ? ScoringParamsG::COST_FIRST_COMPLETION
                                               : ScoringParamsG::COST_COMPLETION;
            return cost;
        }

        float getSkipCost(const DicTraverseSession *const traverseSession,
                          const DicNode *const dicNode) const {
            const int pointIndex = dicNode->getInputIndex(0);

            float probability = traverseSession->getProximityInfoState(0)->getProbability(pointIndex, NOT_AN_INDEX);

            return probability;
        }

        float getOmissionCost(const DicNode *const parentDicNode, const DicNode *const dicNode) const {
            return (dicNode->isSameNodeCodePoint(parentDicNode))
                   ? ScoringParamsG::OMISSION_COST_SAME_CHAR
                   : ScoringParamsG::OMISSION_COST;
        }

        float getMatchedCost(const DicTraverseSession *const traverseSession,
                             const DicNode *const dicNode, DicNode_InputStateG *inputStateG) const {
            const int pointIndex = dicNode->getInputIndex(0);
            const int baseChar = CharUtils::toBaseLowerCase(dicNode->getNodeCodePoint());
            const int keyId = traverseSession->getProximityInfo()->getKeyIndexOf(baseChar);
            const float probability = traverseSession-> getProximityInfoState(0)->getProbability(pointIndex, keyId);

            return probability;
        }

        bool isProximityDicNode(const DicTraverseSession *const traverseSession,
                                const DicNode *const dicNode) const {
            const int pointIndex = dicNode->getInputIndex(0);
            const int primaryCodePoint = CharUtils::toBaseLowerCase(
                    traverseSession->getProximityInfoState(0)->getPrimaryCodePointAt(pointIndex));
            const int dicNodeChar = CharUtils::toBaseLowerCase(dicNode->getNodeCodePoint());
            return primaryCodePoint != dicNodeChar;
        }

        float getTranspositionCost(const DicTraverseSession *const traverseSession,
                                   const DicNode *const parentDicNode, const DicNode *const dicNode) const {
            const int16_t parentPointIndex = parentDicNode->getInputIndex(0);
            const int prevCodePoint = parentDicNode->getNodeCodePoint();
            const float distance1 = traverseSession->getProximityInfoState(0)->getPointToKeyLength(
                    parentPointIndex + 1, CharUtils::toBaseLowerCase(prevCodePoint));
            const int codePoint = dicNode->getNodeCodePoint();
            const float distance2 = traverseSession->getProximityInfoState(0)->getPointToKeyLength(
                    parentPointIndex, CharUtils::toBaseLowerCase(codePoint));
            const float distance = distance1 + distance2;
            const float weightedLengthDistance =
                    distance * ScoringParams::DISTANCE_WEIGHT_LENGTH;
            return ScoringParams::TRANSPOSITION_COST + weightedLengthDistance;
        }

        float getInsertionCost(const DicTraverseSession *const traverseSession,
                               const DicNode *const parentDicNode, const DicNode *const dicNode) const {
            const int16_t insertedPointIndex = parentDicNode->getInputIndex(0);
            const int prevCodePoint = traverseSession->getProximityInfoState(0)->getPrimaryCodePointAt(
                    insertedPointIndex);
            const int currentCodePoint = dicNode->getNodeCodePoint();
            const bool sameCodePoint = prevCodePoint == currentCodePoint;
            const bool existsAdjacentProximityChars = traverseSession->getProximityInfoState(0)
                    ->existsAdjacentProximityChars(insertedPointIndex);
            const float dist = traverseSession->getProximityInfoState(0)->getPointToKeyLength(
                    insertedPointIndex + 1, CharUtils::toBaseLowerCase(dicNode->getNodeCodePoint()));
            const float weightedDistance = dist * ScoringParams::DISTANCE_WEIGHT_LENGTH;
            const bool singleChar = dicNode->getNodeCodePointCount() == 1;
            float cost = (singleChar ? ScoringParams::INSERTION_COST_FIRST_CHAR : 0.0f);
            if (sameCodePoint) {
                cost += ScoringParams::INSERTION_COST_SAME_CHAR;
            } else if (existsAdjacentProximityChars) {
                cost += ScoringParams::INSERTION_COST_PROXIMITY_CHAR;
            } else {
                cost += ScoringParams::INSERTION_COST;
            }
            return cost + weightedDistance;
        }

        float getSpaceOmissionCost(const DicTraverseSession *const traverseSession,
                                   const DicNode *const dicNode, DicNode_InputStateG *inputStateG) const {
            const float cost = ScoringParams::SPACE_OMISSION_COST;
            return cost * traverseSession->getMultiWordCostMultiplier();
        }

        float getNewWordBigramLanguageCost(const DicTraverseSession *const traverseSession,
                                           const DicNode *const dicNode,
                                           MultiBigramMap *const multiBigramMap) const {
            return DicNodeUtils::getBigramNodeImprobability(
                    traverseSession->getDictionaryStructurePolicy(),
                    dicNode, multiBigramMap) * ScoringParams::DISTANCE_WEIGHT_LANGUAGE;
        }

        float getTerminalInsertionCost(const DicTraverseSession *const traverseSession,
                                       const DicNode *const dicNode) const {
            const int inputIndex = dicNode->getInputIndex(0);
            const int inputSize = traverseSession->getInputSize();
            ASSERT(inputIndex < inputSize);
            // TODO: Implement more efficient logic
            return  ScoringParams::TERMINAL_INSERTION_COST * (inputSize - inputIndex);
        }

        AK_FORCE_INLINE bool needsToNormalizeCompoundDistance() const {
            return false;
        }

        AK_FORCE_INLINE float getAdditionalProximityCost() const {
            return ScoringParams::ADDITIONAL_PROXIMITY_COST;
        }

        AK_FORCE_INLINE float getSubstitutionCost() const {
            return ScoringParams::SUBSTITUTION_COST;
        }

        AK_FORCE_INLINE float getSpaceSubstitutionCost(const DicTraverseSession *const traverseSession,
                                                       const DicNode *const dicNode) const {
            const int inputIndex = dicNode->getInputIndex(0);
            const float distanceToSpaceKey = traverseSession->getProximityInfoState(0)
                    ->getPointToKeyLength(inputIndex, KEYCODE_SPACE);
            const float cost = ScoringParams::SPACE_SUBSTITUTION_COST * distanceToSpaceKey;
            return cost * traverseSession->getMultiWordCostMultiplier();
        }

        ErrorTypeUtils::ErrorType getErrorType(const CorrectionType correctionType,
                                               const DicTraverseSession *const traverseSession,
                                               const DicNode *const parentDicNode, const DicNode *const dicNode) const;

    private:
        DISALLOW_COPY_AND_ASSIGN(GestureWeighting);
        static const GestureWeighting sInstance;

        GestureWeighting() {}
        ~GestureWeighting() {}
    };
} // namespace latinime


#endif //GOOGLEKEYBOARDV7_GESTURE_WEIGHTING_H
