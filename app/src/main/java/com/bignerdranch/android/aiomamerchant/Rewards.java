package com.bignerdranch.android.aiomamerchant;

public class Rewards {

    public String rewardID;
    public String rewardTitle;
    public int pointsRequired;
    public String rewardDesc;
    public String rewardPolicy;
    public String rewardIconUrl;


    public Rewards(String s){

    }

    public Rewards(String rewardID, String rewardTitle, int pointsRequired, String rewardDesc, String rewardPolicy, String rewardIconUrl) {
        this.rewardID = rewardID;
        this.rewardDesc = rewardDesc;
        this.rewardTitle = rewardTitle;
        this.pointsRequired = pointsRequired;
        this.rewardPolicy = rewardPolicy;
        this.rewardIconUrl = rewardIconUrl;
    }

    public String getRewardID() {
        return rewardID;
    }

    public void setRewardID(String rewardID) {
        this.rewardID = rewardID;
    }

    public String getRewardTitle() {
        return rewardTitle;
    }

    public void setRewardTitle(String rewardTitle) {
        this.rewardTitle = rewardTitle;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    public String getRewardDesc() {
        return rewardDesc;
    }

    public void setRewardDesc(String rewardDesc) {
        this.rewardDesc = rewardDesc;
    }

    public String getRewardPolicy() {
        return rewardPolicy;
    }

    public void setRewardPolicy(String rewardPolicy) {
        this.rewardPolicy = rewardPolicy;
    }

    public String getRewardIconUrl() {
        return rewardIconUrl;
    }

    public void setRewardIconUrl(String rewardIconUrl) {
        this.rewardIconUrl = rewardIconUrl;
    }
}
