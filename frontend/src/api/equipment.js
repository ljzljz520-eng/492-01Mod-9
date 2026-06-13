import request from '@/utils/request'

export const equipmentApi = {
  save(data) {
    return request({
      url: '/equipment',
      method: 'post',
      data
    })
  },
  update(id, data) {
    return request({
      url: `/equipment/${id}`,
      method: 'put',
      data
    })
  },
  delete(id) {
    return request({
      url: `/equipment/${id}`,
      method: 'delete'
    })
  },
  getById(id) {
    return request({
      url: `/equipment/${id}`,
      method: 'get'
    })
  },
  list() {
    return request({
      url: '/equipment/list',
      method: 'get'
    })
  },
  page(params) {
    return request({
      url: '/equipment/page',
      method: 'get',
      params
    })
  }
}
