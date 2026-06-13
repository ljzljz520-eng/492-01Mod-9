import request from '@/utils/request'

export const workerApi = {
  save(data) {
    return request({
      url: '/worker',
      method: 'post',
      data
    })
  },
  update(id, data) {
    return request({
      url: `/worker/${id}`,
      method: 'put',
      data
    })
  },
  delete(id) {
    return request({
      url: `/worker/${id}`,
      method: 'delete'
    })
  },
  getById(id) {
    return request({
      url: `/worker/${id}`,
      method: 'get'
    })
  },
  getByIdCard(idCard) {
    return request({
      url: `/worker/idCard/${idCard}`,
      method: 'get'
    })
  },
  page(params) {
    return request({
      url: '/worker/page',
      method: 'get',
      params
    })
  }
}
