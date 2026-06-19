<template>
  <div class="resignation-page">
    <van-tabs v-model:active="activeTab" sticky>
      <van-tab title="待检查" name="pending" />
      <van-tab title="待审核" name="checked" />
      <van-tab title="已完成" name="completed" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="fetchApplications"
      >
        <van-cell-group inset class="mt-3">
          <van-cell
            v-for="app in applicationList"
            :key="app.id"
            :title="app.workerName"
            :label="app.applicationNo"
            is-link
            @click="showDetail(app)"
          >
            <template #right-icon>
              <div class="text-right">
                <div class="text-sm font-bold text-red-500">
                  ¥{{ app.refundAmount }}
                </div>
                <van-tag :type="getStatusType(app.applicationStatus)" size="small">
                  {{ getStatusText(app.applicationStatus) }}
                </van-tag>
              </div>
            </template>
          </van-cell>
        </van-cell-group>
      </van-list>
    </van-pull-refresh>

    <van-popup v-model:show="showDetailPopup" round position="right" :style="{ width: '90%' }">
      <div class="h-full flex flex-col">
        <div class="p-4 border-b flex justify-between items-center">
          <h3 class="text-lg font-bold">申请详情</h3>
          <van-button size="small" @click="showDetailPopup = false">关闭</van-button>
        </div>
        <div class="flex-1 overflow-auto p-4">
          <van-cell-group inset>
            <van-cell title="申请单号" :value="currentApplication?.applicationNo" />
            <van-cell title="工人姓名" :value="currentApplication?.workerName" />
            <van-cell title="离职类型">
              <template #right-icon>
                <van-tag :type="currentApplication?.resignationType === 'transfer' ? 'success' : 'danger'">
                  {{ currentApplication?.resignationType === 'transfer' ? '转项目' : '完全离职' }}
                </van-tag>
              </template>
            </van-cell>
            <van-cell title="目标项目" :value="currentApplication?.targetProjectName || '-'" />
            <van-cell title="押金总额" :value="`¥${currentApplication?.totalDeposit}`" />
            <van-cell title="扣款总额" :value="`¥${currentApplication?.deductAmount}`" />
            <van-cell title="应退金额">
              <template #right-icon>
                <span class="text-red-500 font-bold">¥{{ currentApplication?.refundAmount }}</span>
              </template>
            </van-cell>
            <van-cell title="申请状态">
              <template #right-icon>
                <van-tag :type="getStatusType(currentApplication?.applicationStatus)">
                  {{ getStatusText(currentApplication?.applicationStatus) }}
                </van-tag>
              </template>
            </van-cell>
          </van-cell-group>

          <h4 class="font-bold mt-4 mb-2">装备归还明细</h4>
          <van-cell-group inset v-if="currentApplication?.equipmentReturns">
            <van-cell
              v-for="item in currentApplication.equipmentReturns"
              :key="item.id"
              :title="item.equipmentName"
              :label="item.equipmentCode"
            >
              <template #right-icon>
                <div class="text-right">
                  <van-tag :type="getReturnStatusType(item.returnStatus)" size="small">
                    {{ getReturnStatusText(item.returnStatus) }}
                  </van-tag>
                  <div v-if="item.deductAmount > 0" class="text-xs text-red-500 mt-1">
                    扣款: ¥{{ item.deductAmount }}
                  </div>
                </div>
              </template>
            </van-cell>
          </van-cell-group>
        </div>

        <div class="p-4 border-t">
          <template v-if="currentApplication?.applicationStatus === 'pending'">
            <van-button type="primary" block @click="startCheck">
              开始检查
            </van-button>
          </template>
          <template v-else-if="currentApplication?.applicationStatus === 'checked'">
            <van-button type="success" block @click="showFinanceAudit">
              财务审核
            </van-button>
          </template>
          <template v-else-if="currentApplication?.applicationStatus === 'pending_finance'">
            <div class="flex gap-3">
              <van-button type="warning" block @click="showReject">
                驳回
              </van-button>
              <van-button type="primary" block @click="showConfirmRefund">
                确认退款
              </van-button>
            </div>
          </template>
        </div>
      </div>
    </van-popup>

    <van-popup v-model:show="showCheckPopup" round position="right" :style="{ width: '90%' }">
      <div class="h-full flex flex-col">
        <div class="p-4 border-b flex justify-between items-center">
          <h3 class="text-lg font-bold">班组长检查</h3>
          <van-button size="small" @click="showCheckPopup = false">取消</van-button>
        </div>
        <div class="flex-1 overflow-auto p-4">
          <van-form @submit="submitCheck">
            <div v-for="(item, index) in checkForm.equipmentReturns" :key="item.id" class="mb-4">
              <van-cell-group inset>
                <van-cell :title="item.equipmentName" :label="item.equipmentCode" />
                <van-field
                  v-model="item.returnStatus"
                  is-link
                  name="returnStatus"
                  label="归还状态"
                  placeholder="请选择"
                  readonly
                  @click="showReturnStatusPicker(index)"
                />
                <van-field
                  v-if="item.returnStatus === 'lost' || item.returnStatus === 'damaged'"
                  v-model="item.deductAmount"
                  type="digit"
                  label="扣款金额"
                  placeholder="请输入扣款金额"
                  :rules="[{ required: true, message: '请输入扣款金额' }]"
                />
                <van-field
                  v-if="item.returnStatus === 'lost' || item.returnStatus === 'damaged'"
                  v-model="item.responsibilityNote"
                  type="textarea"
                  label="责任说明"
                  placeholder="请填写责任说明"
                  autosize
                  :rules="[{ required: true, message: '请填写责任说明' }]"
                />
                <van-uploader
                  v-if="item.returnStatus === 'lost' || item.returnStatus === 'damaged'"
                  v-model="item.photoFiles"
                  multiple
                  max-count="9"
                  :max-size="10 * 1024 * 1024"
                  label="上传照片"
                />
              </van-cell-group>
            </div>

            <van-field
              v-model="checkForm.remark"
              type="textarea"
              label="备注"
              placeholder="请输入备注"
              autosize
            />

            <div class="mt-4">
              <van-button round block type="primary" native-type="submit">
                提交检查
              </van-button>
            </div>
          </van-form>
        </div>
      </div>
    </van-popup>

    <van-popup v-model:show="showStatusPicker" round position="bottom">
      <van-picker
        :columns="returnStatusColumns"
        @confirm="onReturnStatusConfirm"
        @cancel="showStatusPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showFinancePopup" round position="bottom" :style="{ height: '50%' }">
      <div class="p-4">
        <h3 class="text-lg font-bold mb-4">财务审核</h3>
        <van-form @submit="submitFinanceAudit">
          <van-field
            v-model="financeForm.remark"
            type="textarea"
            label="审核意见"
            placeholder="请输入审核意见"
            autosize
          />
          <div class="mt-4">
            <van-button round block type="primary" native-type="submit">
              提交审核
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <van-popup v-model:show="showRejectPopup" round position="bottom" :style="{ height: '40%' }">
      <div class="p-4">
        <h3 class="text-lg font-bold mb-4">驳回申请</h3>
        <van-form @submit="submitReject">
          <van-field
            v-model="rejectForm.rejectReason"
            type="textarea"
            label="驳回原因"
            placeholder="请输入驳回原因"
            autosize
            :rules="[{ required: true, message: '请输入驳回原因' }]"
          />
          <div class="mt-4">
            <van-button round block type="danger" native-type="submit">
              确认驳回
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <van-popup v-model:show="showRefundPopup" round position="bottom" :style="{ height: '50%' }">
      <div class="p-4">
        <h3 class="text-lg font-bold mb-4">确认退款</h3>
        <van-form @submit="submitRefund">
          <van-cell-group inset>
            <van-cell title="应退金额" :value="`¥${currentApplication?.refundAmount}`" />
          </van-cell-group>
          <van-field
            v-model="refundForm.actualRefundAmount"
            type="digit"
            label="实际退款"
            placeholder="请输入实际退款金额"
            :rules="[{ required: true, message: '请输入实际退款金额' }]"
          />
          <van-field
            v-model="refundForm.refundVoucher"
            label="退款凭证"
            placeholder="请输入退款凭证号"
            :rules="[{ required: true, message: '请输入退款凭证号' }]"
          />
          <van-field
            v-model="refundForm.remark"
            type="textarea"
            label="备注"
            placeholder="请输入备注"
            autosize
          />
          <div class="mt-4">
            <van-button round block type="primary" native-type="submit">
              确认退款
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { resignationApi } from '@/api/resignation'
import { fileApi } from '@/api/file'

const activeTab = ref('pending')
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const applicationList = ref([])
const currentApplication = ref(null)
const showDetailPopup = ref(false)
const showCheckPopup = ref(false)
const showStatusPicker = ref(false)
const showFinancePopup = ref(false)
const showRejectPopup = ref(false)
const showRefundPopup = ref(false)
const currentCheckIndex = ref(0)
const checkForm = ref({
  equipmentReturns: [],
  remark: ''
})
const financeForm = ref({ remark: '' })
const rejectForm = ref({ rejectReason: '' })
const refundForm = ref({
  actualRefundAmount: '',
  refundVoucher: '',
  remark: ''
})

const returnStatusColumns = [
  { text: '已归还', value: 'returned' },
  { text: '转项目保留', value: 'retained' },
  { text: '遗失', value: 'lost' },
  { text: '损坏', value: 'damaged' }
]

const getTabStatus = () => {
  if (activeTab.value === 'pending') return 'pending'
  if (activeTab.value === 'checked') return 'checked,pending_finance'
  if (activeTab.value === 'completed') return 'refunded,rejected'
  return ''
}

const fetchApplications = async () => {
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      applicationStatus: getTabStatus()
    }
    const res = await resignationApi.page(params)
    if (res.code === 200) {
      if (refreshing.value) {
        applicationList.value = []
        refreshing.value = false
      }
      applicationList.value = [...applicationList.value, ...(res.data?.records || [])]
      loading.value = false
      if (applicationList.value.length >= (res.data?.total || 0)) {
        finished.value = true
      } else {
        pageNum.value++
      }
    }
  } catch (e) {
    loading.value = false
    showToast('获取数据失败')
  }
}

const onRefresh = () => {
  pageNum.value = 1
  finished.value = false
  fetchApplications()
}

const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    checked: 'primary',
    pending_finance: 'primary',
    refunded: 'success',
    rejected: 'danger'
  }
  return types[status] || 'default'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待班组长检查',
    checked: '待财务审核',
    pending_finance: '待财务退款',
    refunded: '已退款',
    rejected: '已驳回'
  }
  return texts[status] || status
}

const getReturnStatusType = (status) => {
  const types = {
    returned: 'success',
    retained: 'primary',
    lost: 'danger',
    damaged: 'warning'
  }
  return types[status] || 'default'
}

const getReturnStatusText = (status) => {
  const texts = {
    returned: '已归还',
    retained: '转项目保留',
    lost: '遗失',
    damaged: '损坏'
  }
  return texts[status] || status
}

const showDetail = async (app) => {
  try {
    const res = await resignationApi.getDetail(app.id)
    if (res.code === 200) {
      currentApplication.value = res.data
      showDetailPopup.value = true
    }
  } catch (e) {
    showToast('获取详情失败')
  }
}

const startCheck = () => {
  checkForm.value = {
    equipmentReturns: currentApplication.value.equipmentReturns.map(item => ({
      id: item.id,
      equipmentId: item.equipmentId,
      equipmentName: item.equipmentName,
      equipmentCode: item.equipmentCode,
      returnStatus: item.returnStatus || 'returned',
      deductAmount: item.deductAmount || 0,
      responsibilityNote: item.responsibilityNote || '',
      photoIds: item.photoIds || [],
      photoFiles: []
    })),
    remark: ''
  }
  showDetailPopup.value = false
  showCheckPopup.value = true
}

const showReturnStatusPicker = (index) => {
  currentCheckIndex.value = index
  showStatusPicker.value = true
}

const onReturnStatusConfirm = ({ selectedOptions }) => {
  checkForm.value.equipmentReturns[currentCheckIndex.value].returnStatus = selectedOptions[0].value
  showStatusPicker.value = false
}

const submitCheck = async () => {
  try {
    for (const item of checkForm.value.equipmentReturns) {
      if ((item.returnStatus === 'lost' || item.returnStatus === 'damaged') && item.photoFiles?.length > 0) {
        const photoIds = []
        for (const file of item.photoFiles) {
          if (!file.status || file.status === 'uploading') {
            const res = await fileApi.upload(file.file)
            if (res.code === 200) {
              photoIds.push(res.data)
            }
          }
        }
        item.photoIds = [...(item.photoIds || []), ...photoIds]
      }
    }

    await showConfirmDialog({
      title: '确认提交',
      message: '检查完成后将提交财务审核，确认提交吗？'
    })

    const res = await resignationApi.teamLeaderCheck(currentApplication.value.id, checkForm.value)
    if (res.code === 200) {
      showToast('检查已提交')
      showCheckPopup.value = false
      onRefresh()
    }
  } catch (e) {
    if (e !== 'cancel') {
      showToast(e.message || '提交失败')
    }
  }
}

const showFinanceAudit = () => {
  financeForm.value = { remark: '' }
  showFinancePopup.value = true
}

const submitFinanceAudit = async () => {
  try {
    await showConfirmDialog({
      title: '确认审核',
      message: '审核通过后将进入退款环节，确认提交吗？'
    })

    const res = await resignationApi.financeAudit(currentApplication.value.id, financeForm.value)
    if (res.code === 200) {
      showToast('审核已提交')
      showFinancePopup.value = false
      showDetailPopup.value = false
      onRefresh()
    }
  } catch (e) {
    if (e !== 'cancel') {
      showToast(e.message || '提交失败')
    }
  }
}

const showReject = () => {
  rejectForm.value = { rejectReason: '' }
  showRejectPopup.value = true
}

const submitReject = async () => {
  try {
    await showConfirmDialog({
      title: '确认驳回',
      message: '确定要驳回该申请吗？',
      confirmButtonText: '驳回',
      confirmButtonColor: '#ee0a24'
    })

    const res = await resignationApi.reject(currentApplication.value.id, rejectForm.value)
    if (res.code === 200) {
      showToast('已驳回')
      showRejectPopup.value = false
      showDetailPopup.value = false
      onRefresh()
    }
  } catch (e) {
    if (e !== 'cancel') {
      showToast(e.message || '操作失败')
    }
  }
}

const showConfirmRefund = () => {
  refundForm.value = {
    actualRefundAmount: currentApplication.value.refundAmount,
    refundVoucher: '',
    remark: ''
  }
  showRefundPopup.value = true
}

const submitRefund = async () => {
  try {
    await showConfirmDialog({
      title: '确认退款',
      message: '确认退款后将更新工人状态和装备库存，确认提交吗？'
    })

    const res = await resignationApi.confirmRefund(currentApplication.value.id, refundForm.value)
    if (res.code === 200) {
      showToast('退款已确认')
      showRefundPopup.value = false
      showDetailPopup.value = false
      onRefresh()
    }
  } catch (e) {
    if (e !== 'cancel') {
      showToast(e.message || '操作失败')
    }
  }
}

onMounted(() => {
  fetchApplications()
})
</script>

<style scoped>
.resignation-page {
  padding-bottom: 20px;
}
.mt-3 {
  margin-top: 12px;
}
.mt-4 {
  margin-top: 16px;
}
.mb-2 {
  margin-bottom: 8px;
}
.mb-4 {
  margin-bottom: 16px;
}
.p-4 {
  padding: 16px;
}
.text-right {
  text-align: right;
}
.text-sm {
  font-size: 14px;
}
.text-xs {
  font-size: 12px;
}
.text-red-500 {
  color: #ee0a24;
}
.font-bold {
  font-weight: bold;
}
.h-full {
  height: 100%;
}
.flex {
  display: flex;
}
.flex-col {
  flex-direction: column;
}
.flex-1 {
  flex: 1;
}
.overflow-auto {
  overflow: auto;
}
.justify-between {
  justify-content: space-between;
}
.items-center {
  align-items: center;
}
.gap-3 {
  gap: 12px;
}
</style>
