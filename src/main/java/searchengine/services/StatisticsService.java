package searchengine.services;

import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.ArrayList;

public interface StatisticsService {
    StatisticsResponse getStatistics();

}
