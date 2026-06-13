import request from '@/utils/request'

export const enterpriseApi = {
  save(data) {
    return request({
      url: '/enterprise',
      method: 'post',
      data
    })
  },
  update(id, data) {
    return request({
      url: `/enterprise/${id}`,
      method: 'put',
      data
    })
  },
  delete(id) {
    return request({
      url: `/enterprise/${id}`,
      method: 'delete'
    })
  },
  getById(id) {
    return request({
      url: `/enterprise/${id}`,
      method: 'get'
    })
  },
  list() {
    return request({
      url: '/enterprise/list',
      method: 'get'
    })
  },
  page(params) {
    return request({
      url: '/enterprise/page',
      method: 'get',
      params
    })
  }
}
