import axios from 'axios'

const API_URL = '/api' // 베이스 URL을 설정합니다.

// 공지사항 등록
export const createNotice = async (request: { title: string; content: string }) => {
  try {
    const response = await axios.post(`${API_URL}/admin/notices`, request)
    return response.data // 공지사항 ID 반환
  } catch (error) {
    console.error('공지사항 등록 실패:', error)
    throw error
  }
}

// 공지사항 목록 조회
export const getAllNotices = async (
  keyword: string | null,
  page: number = 0,
  size: number = 10,
) => {
  try {
    const response = await axios.get(`${API_URL}/notices`, {
      params: {
        keyword: keyword || '',
        page,
        size,
      },
    })
    return response.data // { content: [], totalElements: number }
  } catch (error) {
    console.error('공지사항 목록 조회 실패:', error)
    throw error
  }
}

// 공지사항 상세 조회
export const getNoticeById = async (id: number) => {
  try {
    const response = await axios.get(`${API_URL}/notices/${id}`)
    return response.data // 공지사항 상세 내용 반환
  } catch (error) {
    console.error('공지사항 상세 조회 실패:', error)
    throw error
  }
}

// 공지사항 삭제
export const deleteNotice = async (id: number) => {
  try {
    const response = await axios.delete(`${API_URL}/admin/notices/${id}`)
    return response.data // 성공 메시지 반환
  } catch (error) {
    console.error('공지사항 삭제 실패:', error)
    throw error
  }
}

// 공지사항 수정
export const updateNotice = async (update: { id: number; title: string; content: string }) => {
  try {
    const response = await axios.put(`${API_URL}/admin/notices`, update)
    return response.data // 성공 메시지 반환
  } catch (error) {
    console.error('공지사항 수정 실패:', error)
    throw error
  }
}
