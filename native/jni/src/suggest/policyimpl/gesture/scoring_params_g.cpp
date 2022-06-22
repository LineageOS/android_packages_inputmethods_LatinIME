//
// Created by msj on 2017/3/14.
//

#include "suggest/policyimpl/gesture/scoring_params_g.h"

namespace latinime {
    const float ScoringParamsG::COST_FIRST_COMPLETION = 1.0f;
    const float ScoringParamsG::COST_COMPLETION = 0.5f;
    const float ScoringParamsG::NORMALIZED_SPATIAL_DISTANCE_THRESHOLD_FOR_EDIT = 0.5f;

    const float ScoringParamsG::OMISSION_COST_SAME_CHAR = 0.2f;
    const float ScoringParamsG::OMISSION_COST = 1.5f;

    // 候选项中超出采样长度的长度的惩罚权重
    const float ScoringParamsG::DISTANCE_WEIGHT_EXCEEDING_INPUT_SIZE = 1.0f;



    // 这个是 词频补 的权重，和距离信息的负对数组成最终的得分
    const float ScoringParamsG::DISTANCE_WEIGHT_LANGUAGE = 4.0f;

    // 优先队列的长度，影响遍历词典的 检全率
    const int ScoringParamsG::MAX_CACHE_DIC_NODE_SIZE = 80;
    const int ScoringParamsG::MAX_CACHE_DIC_NODE_SIZE_FOR_SINGLE_POINT = 310;

    // 该点是否跳过的阈值，含义为概率的负对数
    const float ScoringParamsG::THRESHOLD_FOR_SKIP = 1.5f;
} // namespace latinime

