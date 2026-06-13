import request from '@/utils/request'

export const resignationApi = {
  create(data, params) {
    return request({
      url: '/resignation',
      method: 'post',
      data,
      params
    })
  },
  teamLeaderCheck(id, data, params) {
    return request({
      url: `/resignation/teamLeaderCheck/${id}`,
      method: 'post',
      data,
      params
    })
  },
  financeAudit(id, data, params) {
    return request({
      url: `/resignation/financeAudit/${id}`,
      method: 'post',
      data,
      params
    })
  },
  confirmRefund(id, params) {
    return request({
      url: `/resignation/confirmRefund/${id}`,
      method: 'post',
      params
    })
  },
  reject(id, params) {
    return request({
      url: `/resignation/reject/${id}`,
      method: 'post',
      params
    })
  },
  getDetail(id) {
    return request({
      url: `/resignation/${id}`,
      method: 'get'
    })
  },
  getEquipmentReturns(id) {
    return request({
      url: `/resignation/${id}/equipmentReturns`,
      method: 'get'
    })
  },
  page(params) {
    return request({
      url: '/resignation/page',
      method: 'get',
      params
    })
  }
}
