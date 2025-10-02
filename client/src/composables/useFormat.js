// composables/useFormat.js
import { computed } from 'vue'

// --- Date Formatting ---

/**
 * 날짜를 "YYYY-MM-DD" 형식으로 포맷
 * Formats a date to "YYYY-MM-DD"
 * @param {import('vue').Ref<string | Date | null> | string | Date | null} inputDate
 * @returns {import('vue').ComputedRef<string>}
 */
export function useDateFormat(inputDate) {
  // Handles both reactive (Ref) and non-reactive inputs
  const valueRef = computed(() =>
    inputDate && typeof inputDate === 'object' && 'value' in inputDate ? inputDate.value : inputDate
  )

  return computed(() => {
    const input = valueRef.value
    if (!input) return ''

    // Ensure input is a string or Date object
    const date = typeof input === 'string' ? new Date(input) : input

    if (isNaN(date.getTime())) return '' // Check for invalid date

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')

    return `${year}-${month}-${day}`
  })
}

// --- Number Formatting ---

/**
 * 숫자를 3자리마다 콤마(,)를 붙여서 포맷
 * Formats a number with commas as thousands separators.
 * @param {import('vue').Ref<number | string | null> | number | string | null} inputNumber
 * @returns {import('vue').ComputedRef<string>}
 */
export function useNumberFormat(inputNumber) {
  const valueRef = computed(() =>
    inputNumber && typeof inputNumber === 'object' && 'value' in inputNumber ? inputNumber.value : inputNumber
  )

  return computed(() => {
    const input = valueRef.value
    if (input === null || input === undefined || input === '') return ''

    const num = Number(input)
    if (isNaN(num)) return ''

    return num.toLocaleString('ko-KR') // e.g., 1,234,567
  })
}

// --- Business Registration Formatting ---

/**
 * 사업자 등록번호를 "XXX-XX-XXXXX" 형식으로 포맷
 * Formats a business registration number to "XXX-XX-XXXXX" (Korean format).
 * @param {import('vue').Ref<string | number | null> | string | number | null} value
 * @returns {import('vue').ComputedRef<string>}
 */
export function useBusinessRegistrationFormat(value) {
  const valueRef = computed(() =>
    value && typeof value === 'object' && 'value' in value ? value.value : value
  )

  return computed(() => {
    const input = valueRef.value
    if (!input) return ''

    // Remove all non-digit characters
    let numbers = input.toString().replace(/\D/g, '')

    // Truncate to a maximum of 10 digits
    if (numbers.length > 10) {
      numbers = numbers.slice(0, 10)
    }

    if (numbers.length < 4) return numbers
    if (numbers.length < 6) return numbers.replace(/(\d{3})(\d+)/, '$1-$2')

    // Full format: XXX-XX-XXXXX
    return numbers.replace(/(\d{3})(\d{2})(\d{5})/, '$1-$2-$3')
  })
}

// --- Phone Number Formatting ---

/**
 * 전화번호를 한국 형식에 맞게 포맷
 * Formats a phone number to the Korean standard (e.g., 010-1234-5678, 02-1234-5678).
 * @param {import('vue').Ref<string | number | null> | string | number | null} value
 * @returns {import('vue').ComputedRef<string>}
 */
export function usePhoneFormat(value) {
  const valueRef = computed(() =>
    value && typeof value === 'object' && 'value' in value ? value.value : value
  )

  return computed(() => {
    const input = valueRef.value
    if (!input) return ''

    // Remove all non-digit characters
    let numbers = input.toString().replace(/\D/g, '')

    // Truncate to a maximum of 11 digits
    if (numbers.length > 11) {
      numbers = numbers.slice(0, 11)
    }

    if (numbers.length < 4) return numbers

    // 11 digits: 010-XXXX-XXXX (Mobile/New Regional)
    if (numbers.length === 11) {
      return numbers.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3')
    }

    // 10 digits
    if (numbers.length === 10) {
      // 02-XXXX-XXXX (Seoul area code)
      if (numbers.startsWith('02')) {
        return numbers.replace(/(\d{2})(\d{4})(\d{4})/, '$1-$2-$3')
      }
      // XXX-XXX-XXXX (Other regional area codes)
      return numbers.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3')
    }

    // Numbers > 7 digits (Fallback for 7, 8, or 9 digits)
    if (numbers.length > 7) {
      // General pattern: (Area code/First part) - (Middle) - (Last 4 digits)
      return numbers.replace(/(\d{2,3})(\d{3,4})(\d{4})/, '$1-$2-$3')
    }

    // Numbers 4 to 7 digits (e.g., local number)
    return numbers.replace(/(\d{3})(\d+)/, '$1-$2')
  })
}
