//
// Created by msj on 2017/3/14.
//

#ifndef GOOGLEKEYBOARDV7_SCORING_PARAMS_G_H
#define GOOGLEKEYBOARDV7_SCORING_PARAMS_G_H

#include "defines.h"

namespace latinime {
    class ScoringParamsG {
    public:
        static const float COST_FIRST_COMPLETION;
        static const float COST_COMPLETION;
        static const float NORMALIZED_SPATIAL_DISTANCE_THRESHOLD_FOR_EDIT;

        static const float OMISSION_COST_SAME_CHAR;
        static const float OMISSION_COST;

        static const float DISTANCE_WEIGHT_EXCEEDING_INPUT_SIZE;

        static const float DISTANCE_WEIGHT_LANGUAGE;

        static const int MAX_CACHE_DIC_NODE_SIZE;
        static const int MAX_CACHE_DIC_NODE_SIZE_FOR_SINGLE_POINT;

        static const float THRESHOLD_FOR_SKIP;
    private:
        DISALLOW_IMPLICIT_CONSTRUCTORS(ScoringParamsG);
    };
} // namespace latinime



#endif //GOOGLEKEYBOARDV7_SCORING_PARAMS_G_H
