package com.syfe.finance_manager.dto;

import java.util.List;

public class GoalsListResponse {
    private List<GoalResponse> goals;

    public GoalsListResponse() {}

    public GoalsListResponse(List<GoalResponse> goals) {
        this.goals = goals;
    }

    public List<GoalResponse> getGoals() {
        return goals;
    }

    public void setGoals(List<GoalResponse> goals) {
        this.goals = goals;
    }
}
