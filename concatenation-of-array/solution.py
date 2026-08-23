# Time:  O(n)
# Space: O(n)

class Solution:
    def getConcatenation(self, nums: List[int]) -> List[int]:
        concat_lst = []
        concat_lst.extend(nums)
        concat_lst.extend(nums)
        return concat_lst
