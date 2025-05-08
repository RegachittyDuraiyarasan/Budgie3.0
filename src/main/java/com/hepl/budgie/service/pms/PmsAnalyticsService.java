package com.hepl.budgie.service.pms;

import java.util.List;
import java.util.Map;

public interface PmsAnalyticsService {
    List<Map<String, String>> fetchChartData(String pmsYear, String levelName);

}
