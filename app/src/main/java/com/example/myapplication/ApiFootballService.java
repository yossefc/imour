package com.example.myapplication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiFootballService {

    @GET("leagues")
    Call<LeaguesResponse> getLeagues(
            @Query("country") String country,
            @Query("search") String search,
            @Query("current") String current
    );

    @GET("fixtures")
    Call<FixturesResponse> getFixtures(
            @Query("league") int leagueId,
            @Query("season") int season,
            @Query("next") int next
    );

    @GET("odds")
    Call<OddsResponse> getOdds(
            @Query("league") int leagueId,
            @Query("season") int season
            // אפשר להוסיף פילטרים (bookmaker/bet) בהמשך
    );

    @GET("fixtures")
    Call<FixturesResponse> getFixtureById(@Query("id") int fixtureId);
}
