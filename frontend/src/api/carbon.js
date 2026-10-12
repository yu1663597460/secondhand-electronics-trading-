import request from '../utils/request'

export const getCarbonAccount = () => request.get('/carbon/account')
export const dailySign = () => request.post('/carbon/sign')
export const getCarbonRecords = (page = 0, size = 8) =>
  request.get('/carbon/records', { params: { page, size } })
