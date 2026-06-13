import request from '@/utils/request'

export const workerEquipmentApi = {
  distribute(params) {
    return request({
      url: '/workerEquipment/distribute',
      method: 'post',
      params
    })
  },
  returnEquipment(id) {
    return request({
      url: `/workerEquipment/return/${id}`,
      method: 'post'
    })
  },
  update(id, data) {
    return request({
      url: `/workerEquipment/${id}`,
      method: 'put',
      data
    })
  },
  delete(id) {
    return request({
      url: `/workerEquipment/${id}`,
      method: 'delete'
    })
  },
  getById(id) {
    return request({
      url: `/workerEquipment/${id}`,
      method: 'get'
    })
  },
  getByWorkerId(workerId) {
    return request({
      url: `/workerEquipment/byWorker/${workerId}`,
      method: 'get'
    })
  },
  getActiveEquipment(workerId) {
    return request({
      url: `/workerEquipment/active/${workerId}`,
      method: 'get'
    })
  },
  page(params) {
    return request({
      url: '/workerEquipment/page',
      method: 'get',
      params
    })
  }
}
