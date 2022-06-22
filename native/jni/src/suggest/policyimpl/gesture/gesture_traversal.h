//
// Created by msj on 2017/3/9.
//

#ifndef GOOGLEKEYBOARDV7_GESTURE_TRAVERSAL_H
#define GOOGLEKEYBOARDV7_GESTURE_TRAVERSAL_H


#include <cstdint>

#include "defines.h"
#include "suggest/core/dicnode/dic_node.h"
#include "suggest/core/dicnode/dic_node_vector.h"
#include "suggest/core/layout/proximity_info_state.h"
#include "suggest/core/layout/proximity_info_utils.h"
#include "suggest/core/policy/traversal.h"
#include "suggest/core/session/dic_traverse_session.h"
#include "suggest/core/suggest_options.h"
#include "suggest/policyimpl/typing/scoring_params.h"
#include "suggest/policyimpl/gesture/scoring_params_g.h"
#include "utils/char_utils.h"

namespace latinime {
    class GestureTraversal : public Traversal {
    public:
        static const GestureTraversal *getInstance() { return &sInstance; }

        int getMaxPointerCount() const {
            return MAX_POINTER_COUNT_G;
        }

        float getMaxSpatialDistance() const {
            return ScoringParams::MAX_SPATIAL_DISTANCE;
        }

        int getDefaultExpandDicNodeSize() const {
            return DicNodeVector::DEFAULT_NODES_SIZE_FOR_OPTIMIZATION;
        }

        bool allowsErrorCorrections(const DicNode *const dicNode) const {
            return dicNode->getNormalizedSpatialDistance()
                   < ScoringParamsG::NORMALIZED_SPATIAL_DISTANCE_THRESHOLD_FOR_EDIT;
        }

        bool isSkip(const DicTraverseSession *const traverseSession,
                    const DicNode *const dicNode) const {
            const int inputSize = traverseSession->getInputSize();
            const int point0Index = dicNode->getInputIndex(0);
            if(point0Index < inputSize) {
                const float probability = traverseSession->getProximityInfoState(0)->getProbability(point0Index, NOT_AN_INDEX);
                if(probability < ScoringParamsG::THRESHOLD_FOR_SKIP) {
                    return true;
                }
            }
            return false;
        }

        bool isOmission(const DicTraverseSession *const traverseSession,
                                        const DicNode *const dicNode, const DicNode *const childDicNode,
                                        const bool allowsErrorCorrections) const {
            if (!CORRECT_OMISSION) {
                return false;
            }
            // Note: Always consider intentional omissions (like apostrophes) since they are common.
            const bool canConsiderOmission = allowsErrorCorrections;
            if (!canConsiderOmission) {
                return false;
            }
            const int inputSize = traverseSession->getInputSize();
            // TODO: Don't refer to isCompletion?
            if (dicNode->isCompletion(inputSize)) {
                return false;
            }
            if (dicNode->canBeIntentionalOmission()) {
                return true;
            }
            const int point0Index = dicNode->getInputIndex(0);
            const int currentBaseLowerCodePoint =
                    CharUtils::toBaseLowerCase(childDicNode->getNodeCodePoint());
            const int typedBaseLowerCodePoint =
                    CharUtils::toBaseLowerCase(traverseSession->getProximityInfoState(0)
                                                       ->getPrimaryCodePointAt(point0Index));
            return (currentBaseLowerCodePoint != typedBaseLowerCodePoint);
        }

        ProximityType getProximityType(
                const DicTraverseSession *const traverseSession, const DicNode *const dicNode,
                const DicNode *const childDicNode) const {
            return traverseSession->getProximityInfoState(0)->getProximityTypeG(
                    dicNode->getInputIndex(0), childDicNode->getNodeCodePoint());
        }

        int getMaxCacheSize(const int inputSize, const float weightForLocale) const {
            if (inputSize <= 1) {
                return ScoringParamsG::MAX_CACHE_DIC_NODE_SIZE_FOR_SINGLE_POINT;
            }
            if (weightForLocale < ScoringParams::LOCALE_WEIGHT_THRESHOLD_FOR_SMALL_CACHE_SIZE) {
                return ScoringParams::MAX_CACHE_DIC_NODE_SIZE_FOR_LOW_PROBABILITY_LOCALE;
            }
            return ScoringParamsG::MAX_CACHE_DIC_NODE_SIZE;
        }

        int getTerminalCacheSize() const {
            return MAX_RESULTS;
        }

        bool isPossibleOmissionChildNode(
                const DicTraverseSession *const traverseSession, const DicNode *const parentDicNode,
                const DicNode *const dicNode) const {
            const ProximityType proximityType =
                    getProximityType(traverseSession, parentDicNode, dicNode);
            if (!ProximityInfoUtils::isMatchOrProximityChar(proximityType)) {
                return false;
            }
            return true;
        }













        AK_FORCE_INLINE bool isSpaceSubstitutionTerminal(
                const DicTraverseSession *const traverseSession, const DicNode *const dicNode) const {
            return false;
        }

        AK_FORCE_INLINE bool isSpaceOmissionTerminal(
                const DicTraverseSession *const traverseSession, const DicNode *const dicNode) const {
            return false;
        }

        AK_FORCE_INLINE bool shouldDepthLevelCache(
                const DicTraverseSession *const traverseSession) const {
            const int inputSize = traverseSession->getInputSize();
            return traverseSession->isCacheBorderForTyping(inputSize);
        }

        AK_FORCE_INLINE bool shouldNodeLevelCache(
                const DicTraverseSession *const traverseSession, const DicNode *const dicNode) const {
            return false;
        }

        AK_FORCE_INLINE bool canDoLookAheadCorrection(
                const DicTraverseSession *const traverseSession, const DicNode *const dicNode) const {
            const int inputSize = traverseSession->getInputSize();
            return dicNode->canDoLookAheadCorrection(inputSize);
        }

        AK_FORCE_INLINE bool needsToTraverseAllUserInput() const {
            return true;
        }







        AK_FORCE_INLINE bool isGoodToTraverseNextWord(const DicNode *const dicNode,
                                                      const int probability) const {
            if (probability < ScoringParams::THRESHOLD_NEXT_WORD_PROBABILITY) {
                return false;
            }
            const bool shortCappedWord = dicNode->getNodeCodePointCount()
                                         < ScoringParams::THRESHOLD_SHORT_WORD_LENGTH && dicNode->isFirstCharUppercase();
            return !shortCappedWord
                   || probability >= ScoringParams::THRESHOLD_NEXT_WORD_PROBABILITY_FOR_CAPPED;
        }

    private:
        DISALLOW_COPY_AND_ASSIGN(GestureTraversal);
        static const bool CORRECT_OMISSION;
        static const GestureTraversal sInstance;

        GestureTraversal() {}
        ~GestureTraversal() {}
    };
} // namespace latinime



#endif //GOOGLEKEYBOARDV7_GESTURE_TRAVERSAL_H
