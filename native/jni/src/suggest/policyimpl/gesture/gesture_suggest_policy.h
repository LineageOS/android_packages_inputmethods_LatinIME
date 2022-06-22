//
// Created by msj on 2017/3/9.
//

#ifndef GOOGLEKEYBOARDV7_GESTURE_SUGGEST_POLICY_H
#define GOOGLEKEYBOARDV7_GESTURE_SUGGEST_POLICY_H

#include "defines.h"
#include "suggest/core/policy/suggest_policy.h"
#include "suggest/policyimpl/gesture/gesture_scoring.h"
#include "suggest/policyimpl/gesture/gesture_traversal.h"
#include "suggest/policyimpl/gesture/gesture_weighting.h"

namespace latinime {

    class Scoring;

    class Traversal;

    class Weighting;

    class GestureSuggestPolicy : public SuggestPolicy {
    public:
        static const GestureSuggestPolicy *getInstance() { return &sInstance; }

        GestureSuggestPolicy() { }

        virtual ~GestureSuggestPolicy() { }

        AK_FORCE_INLINE const Traversal *getTraversal() const {
            return GestureTraversal::getInstance();
        }

        AK_FORCE_INLINE const Scoring *getScoring() const {
            return GestureScoring::getInstance();
        }

        AK_FORCE_INLINE const Weighting *getWeighting() const {
            return GestureWeighting::getInstance();
        }

    private:
        DISALLOW_COPY_AND_ASSIGN(GestureSuggestPolicy);

        static const GestureSuggestPolicy sInstance;
    };

} // namespace latinime

#endif //GOOGLEKEYBOARDV7_GESTURE_SUGGEST_POLICY_H
