package com.example.music.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RecommendUtils {

    /**
     * 计算两用户皮尔逊相关系数
     */
    public static double pearsonSimilarity(double[] x, double[] y) {
        double sumXY = 0;
        double sumX = 0;
        double sumY = 0;
        double sumX2 = 0;
        double sumY2 = 0;
        int n = x.length;

        for (int i = 0; i < n; i++) {
            sumXY += x[i] * y[i];
            sumX += x[i];
            sumY += y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }
        double numerator = sumXY - (sumX * sumY / n);
        double denominator = Math.sqrt((sumX2 - sumX * sumX / n) * (sumY2 - sumY * sumY / n));

        if (denominator == 0) {
            return 0;
        }
        return numerator / denominator;
    }

    /**
     * 获取与指定用户最相似的K个用户
     */
    public static List<Integer> getKSimilarUsers(int userIndex, double[][] userRatings, int k) {
        List<Integer> similarUsers = new ArrayList<>();
        double[] targetUser = userRatings[userIndex];
        double[] similarities = new double[userRatings.length];

        for (int i = 0; i < userRatings.length; i++) {
            if (i == userIndex) {
                similarities[i] = -2; // 设置为最小值，避免选择自己
                continue;
            }
            double similarity = pearsonSimilarity(targetUser, userRatings[i]);
            similarities[i] = similarity;
        }

        // 如果用户数量少于k，调整k值
        int actualK = Math.min(k, userRatings.length - 1);

        for (int i = 0; i < actualK; i++) {
            double maxSimilarity = -2;
            int maxIndex = -1;

            for (int j = 0; j < similarities.length; j++) {
                if (similarities[j] > maxSimilarity) {
                    maxSimilarity = similarities[j];
                    maxIndex = j;
                }
            }

            if (maxIndex != -1 && maxSimilarity > 0) { // 只添加正相关的用户
                similarUsers.add(maxIndex);
                similarities[maxIndex] = -2;
            } else {
                break; // 没有更多正相关用户
            }
        }

        return similarUsers;
    }

    /**
     * 计算每首歌曲的热门度（被喜欢的用户数）
     */
    public static double[] calculatePopularity(double[][] userRatings) {
        int itemCount = userRatings[0].length;
        double[] popularity = new double[itemCount];
        
        for (int j = 0; j < itemCount; j++) {
            int likedCount = 0;
            for (int i = 0; i < userRatings.length; i++) {
                if (userRatings[i][j] == 1) {
                    likedCount++;
                }
            }
            popularity[j] = likedCount;
        }
        return popularity;
    }

    /**
     * 计算异常热门惩罚因子
     * 惩罚因子 = 1 / (1 + log(热门度/平均热门度))
     * 这样可以降低过于热门的歌曲的推荐权重
     */
    public static double[] calculatePopularityPenaltyFactors(double[] popularity) {
        int n = popularity.length;
        double[] penaltyFactors = new double[n];
        double avgPopularity = 0;
        
        // 计算平均热门度
        for (double p : popularity) {
            avgPopularity += p;
        }
        avgPopularity /= n;
        if (avgPopularity == 0) avgPopularity = 1; // 避免除零
        
        // 计算惩罚因子
        for (int i = 0; i < n; i++) {
            // 使用对数函数来平滑热门度的影响
            penaltyFactors[i] = 1.0 / (1.0 + Math.log(1 + popularity[i] / avgPopularity));
            // 确保惩罚因子在合理范围内 [0.1, 1.0]
            penaltyFactors[i] = Math.max(0.1, Math.min(1.0, penaltyFactors[i]));
        }
        return penaltyFactors;
    }

    /**
     * 为指定用户推荐物品（原始版本，无惩罚因子）
     */
    public static List<Integer> recommendItems(int userIndex, double[][] userRatings, int k) {
        List<Integer> recommendedItems = new ArrayList<>();
        double[] targetUser = userRatings[userIndex];

        // 获取相似用户，最多取前20个相似用户
        List<Integer> similarUsers = getKSimilarUsers(userIndex, userRatings, Math.min(20, userRatings.length - 1));

        if (similarUsers.isEmpty()) {
            return getRandomRecommendations(userRatings[0].length, k);
        }

        // 计算物品的推荐得分
        double[] itemScores = new double[userRatings[0].length];

        for (Integer similarUserIndex : similarUsers) {
            double similarity = pearsonSimilarity(targetUser, userRatings[similarUserIndex]);
            double[] similarUserRatings = userRatings[similarUserIndex];

            for (int j = 0; j < similarUserRatings.length; j++) {
                // 如果目标用户没有喜欢过该物品，且相似用户喜欢
                if (targetUser[j] == 0 && similarUserRatings[j] == 1) {
                    itemScores[j] += similarity;
                }
            }
        }
        // 根据得分选择top-k物品
        for (int i = 0; i < k; i++) {
            double maxScore = -1;
            int maxIndex = -1;

            for (int j = 0; j < itemScores.length; j++) {
                if (itemScores[j] > maxScore && !recommendedItems.contains(j)) {
                    maxScore = itemScores[j];
                    maxIndex = j;
                }
            }
            if (maxIndex != -1 && maxScore > 0) {
                recommendedItems.add(maxIndex);
            } else {
                break; // 没有更多可推荐物品
            }
        }
        // 如果推荐数量不足，补充随机推荐
        if (recommendedItems.size() < k) {
            List<Integer> randomItems = getRandomRecommendations(userRatings[0].length, k - recommendedItems.size());
            for (Integer item : randomItems) {
                if (!recommendedItems.contains(item)) {
                    recommendedItems.add(item);
                }
            }
        }
        return recommendedItems;
    }

    /**
     * 带异常热门惩罚因子的推荐算法
     * 为了解决传统协同过滤算法中热门歌曲权重过高的问题
     */
    public static List<Integer> recommendItemsWithPopularityPenalty(int userIndex, double[][] userRatings, int k) {
        List<Integer> recommendedItems = new ArrayList<>();
        double[] targetUser = userRatings[userIndex];

        // 获取相似用户，最多取前20个相似用户
        List<Integer> similarUsers = getKSimilarUsers(userIndex, userRatings, Math.min(20, userRatings.length - 1));
        if (similarUsers.isEmpty()) {
            return getRandomRecommendations(userRatings[0].length, k);
        }
        // 计算每首歌曲的热门度
        double[] popularity = calculatePopularity(userRatings);
        // 计算异常热门惩罚因子
        double[] penaltyFactors = calculatePopularityPenaltyFactors(popularity);
        // 计算物品的推荐得分，应用热门惩罚因子
        double[] itemScores = new double[userRatings[0].length];

        for (Integer similarUserIndex : similarUsers) {
            double similarity = pearsonSimilarity(targetUser, userRatings[similarUserIndex]);
            double[] similarUserRatings = userRatings[similarUserIndex];

            for (int j = 0; j < similarUserRatings.length; j++) {
                // 如果目标用户没有喜欢过该物品，且相似用户喜欢
                if (targetUser[j] == 0 && similarUserRatings[j] == 1) {
                    // 应用异常热门惩罚因子
                    itemScores[j] += similarity * penaltyFactors[j];
                }
            }
        }

        // 根据得分选择top-k物品
        for (int i = 0; i < k; i++) {
            double maxScore = Double.NEGATIVE_INFINITY;
            int maxIndex = -1;

            for (int j = 0; j < itemScores.length; j++) {
                if (itemScores[j] > maxScore && !recommendedItems.contains(j)) {
                    maxScore = itemScores[j];
                    maxIndex = j;
                }
            }
            if (maxIndex != -1 && maxScore > 0) { // 只推荐正分物品
                recommendedItems.add(maxIndex);
            } else {
                break; // 没有更多可推荐物品
            }
        }

        // 如果推荐数量不足，补充随机推荐
        if (recommendedItems.size() < k) {
            List<Integer> randomItems = getRandomRecommendations(userRatings[0].length, k - recommendedItems.size());
            for (Integer item : randomItems) {
                if (!recommendedItems.contains(item)) {
                    recommendedItems.add(item);
                }
            }
        }
        return recommendedItems;
    }

    /**
     * 获取随机推荐（当协同过滤无法提供足够推荐时使用）
     */
    private static List<Integer> getRandomRecommendations(int itemCount, int k) {
        List<Integer> randomItems = new ArrayList<>();
        Random random = new Random();

        int actualK = Math.min(k, itemCount);
        for (int i = 0; i < actualK; i++) {
            int randomIndex;
            do {
                randomIndex = random.nextInt(itemCount);
            } while (randomItems.contains(randomIndex));

            randomItems.add(randomIndex);
        }

        return randomItems;
    }
}