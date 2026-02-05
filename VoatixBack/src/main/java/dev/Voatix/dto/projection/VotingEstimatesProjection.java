package dev.Voatix.dto.projection;

public interface VotingEstimatesProjection {
    Long getSurveyId();
    Long getId();
    String getTitle();
    Long getVotesCount();
    Boolean getIsVoted();
}
