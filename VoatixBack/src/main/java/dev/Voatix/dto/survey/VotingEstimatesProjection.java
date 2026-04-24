package dev.Voatix.dto.survey;

public interface VotingEstimatesProjection {
    Long getSurveyId();
    Long getId();
    String getTitle();
    Long getVotesCount();
    Boolean getIsVoted();
}
