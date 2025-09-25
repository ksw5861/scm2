// composables/useFormat.js
import { computed } from 'vue'

/**
 * 날짜를 "YYYY-MM-DD" 형식으로 포맷
 * @param {string | Date | null} inputDate
 * @returns {ComputedRef<string>}
 */
export function useDateFormat(inputDate) {
  return computed(() => {
    if (!inputDate) return ''
    const date = typeof inputDate === 'string' ? new Date(inputDate) : inputDate
    if (isNaN(date)) return ''

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')

    return `${year}-${month}-${day}`
  })
}

/**
 * 숫자를 3자리마다 콤마(,)를 붙여서 포맷
 * @param {number | string | null} inputNumber
 * @returns {ComputedRef<string>}
 */
export function useNumberFormat(inputNumber) {
  return computed(() => {
    if (inputNumber === null || inputNumber === undefined || inputNumber === '') return ''
    const num = Number(inputNumber)
    if (isNaN(num)) return ''

    return num.toLocaleString('ko-KR') // 예: 1,234,567
  })
}
