// Time:  O(M^2.N + M!)
// Space: O(M^2)

class Solution {
    private int M;
    private int N;
    private int maxSum;

    public int maxCompatibilitySum(int[][] students, int[][] mentors) {
        this.M = students.length;
        this.N = students[0].length;
        int[][] scoreMatrix = calculateScoreMatrix(students, mentors);
        backtrack(scoreMatrix, new boolean[M], 0, 0);
        return maxSum;
    }

    void backtrack(int[][] matrix, boolean[] used, int student, int runningSum) {
        if (student == M) {
            maxSum = Math.max(maxSum, runningSum);
            return;
        }

        for (int mentor = 0; mentor < M; mentor++) {
            if (used[mentor]) continue;

            used[mentor] = true;
            backtrack(matrix, used, student + 1, runningSum + matrix[student][mentor]);
            used[mentor] = false;
        } 
    }

    int[][] calculateScoreMatrix(int[][] students, int[][] mentors) {
        int[][] scoreMatrix = new int[M][M];

        for (int i = 0; i < M; i++) {
            int[] studentAnswers = students[i];
            for (int j = 0; j < M; j++) {
                int[] mentorAnswers = mentors[j];
                int score = 0;
                for (int k = 0; k < N; k++) {
                    if (studentAnswers[k] == mentorAnswers[k]) ++score;
                }
                scoreMatrix[i][j] = score;
            }
        }

        return scoreMatrix;
    }
}
