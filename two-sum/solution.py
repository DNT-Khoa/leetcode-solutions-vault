# Time:  O(n)
# Space: O(n)

class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        visited = {}
        for i in range(len(nums)):
            if target - nums[i] in visited:
                return [i, visited[target - nums[i]]]
            visited[nums[i]] = i

