<template>
  <div class="resignation-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">离职押金归还管理</span>
          <el-button type="primary" @click="handleAdd" class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200">
            <el-icon><Plus /></el-icon>
            新建离职申请
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="工人姓名">
          <el-input v-model="searchForm.workerName" placeholder="请输入工人姓名" clearable class="rounded-lg" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="所属企业">
          <el-select v-model="searchForm.enterpriseId" placeholder="请选择企业" clearable style="width: 200px" class="rounded-lg">
            <el-option v-for="item in enterpriseList" :key="item.id" :label="item.enterpriseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属项目">
          <el-select v-model="searchForm.projectId" placeholder="请选择项目" clearable style="width: 200px" class="rounded-lg">
            <el-option v-for="item in projectList" :key="item.id" :label="item.projectName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请状态">
          <el-select v-model="searchForm.applicationStatus" placeholder="请选择状态" clearable style="width: 150px" class="rounded-lg">
            <el-option label="待班组长检查" value="pending" />
            <el-option label="班组长已检查" value="checked" />
            <el-option label="待财务审核" value="pending_finance" />
            <el-option label="已退款" value="refunded" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="离职类型">
          <el-select v-model="searchForm.resignationType" placeholder="请选择类型" clearable style="width: 150px" class="rounded-lg">
            <el-option label="完全离职" value="resign" />
            <el-option label="企业内转项目" value="transfer" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200">查询</el-button>
          <el-button @click="handleReset" class="rounded-lg">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border class="rounded-lg overflow-hidden" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
        <el-table-column prop="applicationNo" label="申请单号" width="160" />
        <el-table-column prop="workerName" label="工人姓名" width="100" />
        <el-table-column prop="currentProjectName" label="当前项目" width="150" />
        <el-table-column prop="targetProjectName" label="目标项目" width="150">
          <template #default="{ row }">{{ row.targetProjectName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="resignationType" label="离职类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.resignationType === 'resign' ? 'danger' : 'warning'" size="small">{{ row.resignationType === 'resign' ? '完全离职' : '转项目' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalDeposit" label="押金总额(元)" width="120">
          <template #default="{ row }">¥{{ row.totalDeposit || '0.00' }}</template>
        </el-table-column>
        <el-table-column prop="deductAmount" label="扣款(元)" width="100">
          <template #default="{ row }"><span class="text-red-500">-¥{{ row.deductAmount || '0.00' }}</span></template>
        </el-table-column>
        <el-table-column prop="refundAmount" label="应退(元)" width="100">
          <template #default="{ row }"><span class="text-green-600">¥{{ row.refundAmount || '0.00' }}</span></template>
        </el-table-column>
        <el-table-column prop="applicationStatus" label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.applicationStatus)" size="small">{{ getStatusText(row.applicationStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.applicationStatus === 'pending'" type="success" size="small" link @click="handleTeamLeaderCheck(row)">班组长检查</el-button>
            <el-button v-if="row.applicationStatus === 'pending_finance'" type="warning" size="small" link @click="handleFinanceAudit(row)">财务审核</el-button>
            <el-button v-if="row.applicationStatus === 'checked'" type="success" size="small" link @click="handleConfirmRefund(row)">确认退款</el-button>
            <el-button v-if="row.applicationStatus === 'pending' || row.applicationStatus === 'pending_finance'" type="danger" size="small" link @click="handleReject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-6 flex justify-end">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" class="rounded-lg" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <el-dialog v-model="createDialogVisible" title="新建离职申请" width="700px" class="rounded-lg" :close-on-click-modal="false">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="120px" class="px-2">
        <el-form-item label="选择工人" prop="workerId">
          <el-select v-model="createForm.workerId" placeholder="请选择工人" filterable class="w-full rounded-lg" @change="handleWorkerChange">
            <el-option v-for="item in workerList" :key="item.id" :label="`${item.workerName} (${item.phone})`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="selectedWorker" label="工人信息">
          <div class="bg-gray-50 p-4 rounded-lg">
            <div class="grid grid-cols-2 gap-2 text-sm">
              <div><span class="text-gray-500">姓名：</span>{{ selectedWorker.workerName }}</div>
              <div><span class="text-gray-500">手机号：</span>{{ selectedWorker.phone }}</div>
              <div><span class="text-gray-500">身份证：</span>{{ selectedWorker.idCard }}</div>
              <div><span class="text-gray-500">押金总额：</span><span class="text-red-500 font-bold">¥{{ selectedWorker.depositAmount || '0.00' }}</span></div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="离职类型" prop="resignationType">
          <el-radio-group v-model="createForm.resignationType" class="w-full">
            <el-radio label="resign">完全离职</el-radio>
            <el-radio label="transfer">企业内转项目</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="createForm.resignationType === 'transfer'" label="目标项目" prop="targetProjectId">
          <el-select v-model="createForm.targetProjectId" placeholder="请选择目标项目" class="w-full rounded-lg">
            <el-option v-for="item in sameEnterpriseProjects" :key="item.id" :label="item.projectName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="最后工作日" prop="lastWorkDate">
          <el-date-picker v-model="createForm.lastWorkDate" type="date" placeholder="请选择最后工作日" class="w-full rounded-lg" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item v-if="selectedWorker" label="在用装备">
          <div class="bg-gray-50 p-4 rounded-lg">
            <el-table :data="workerEquipmentList" size="small" border>
              <el-table-column prop="equipmentName" label="装备名称" />
              <el-table-column prop="equipmentType" label="类型" width="100">
                <template #default="{ row }">{{ getEquipmentTypeText(row.equipmentType) }}</template>
              </el-table-column>
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column prop="depositAmount" label="押金(元)" width="100" />
            </el-table>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.responsibilityNote" type="textarea" :rows="3" placeholder="请输入备注说明" class="rounded-lg" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="createDialogVisible = false" class="rounded-lg">取消</el-button>
          <el-button type="primary" @click="handleCreateSubmit" class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200" :loading="creating">提交申请</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="checkDialogVisible" :title="checkDialogTitle" width="900px" class="rounded-lg" :close-on-click-modal="false">
      <div class="mb-4 bg-blue-50 p-4 rounded-lg">
        <div class="grid grid-cols-2 gap-2 text-sm">
          <div><span class="text-gray-500">申请单号：</span>{{ currentApplication?.applicationNo }}</div>
          <div><span class="text-gray-500">工人姓名：</span>{{ currentApplication?.workerName }}</div>
          <div><span class="text-gray-500">离职类型：</span>{{ currentApplication?.resignationType === 'resign' ? '完全离职' : '企业内转项目' }}</div>
          <div><span class="text-gray-500">当前项目：</span>{{ currentApplication?.currentProjectName }}</div>
          <div v-if="currentApplication?.targetProjectName"><span class="text-gray-500">目标项目：</span>{{ currentApplication?.targetProjectName }}</div>
          <div><span class="text-gray-500">押金总额：</span><span class="text-red-500 font-bold">¥{{ currentApplication?.totalDeposit }}</span></div>
        </div>
      </div>

      <el-alert v-if="currentApplication?.resignationType === 'transfer'" title="转项目说明" type="warning" :closable="false" class="mb-4">
        <template #default>工人转入同一企业的其他项目时，可选择"转项目保留"部分装备，无需退还。</template>
      </el-alert>

      <el-table :data="checkEquipmentList" border class="rounded-lg overflow-hidden mb-4">
        <el-table-column prop="equipmentName" label="装备名称" width="120" />
        <el-table-column prop="equipmentType" label="类型" width="100">
          <template #default="{ row }">{{ getEquipmentTypeText(row.equipmentType) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="depositAmount" label="押金(元)" width="100" />
        <el-table-column label="归还状态" width="180">
          <template #default="{ row }">
            <el-select v-model="row.returnStatus" placeholder="请选择" class="w-full rounded-lg" size="small">
              <el-option label="已归还" value="returned" />
              <el-option v-if="currentApplication?.resignationType === 'transfer'" label="转项目保留" value="retained" />
              <el-option label="遗失" value="lost" />
              <el-option label="损坏" value="damaged" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column v-if="!isFinanceAudit" label="归还状况" width="120">
          <template #default="{ row }">
            <el-select v-model="row.returnCondition" placeholder="请选择" class="w-full rounded-lg" size="small" :disabled="row.returnStatus !== 'returned'">
              <el-option label="良好" value="good" />
              <el-option label="损坏" value="damaged" />
              <el-option label="严重损坏" value="serious_damage" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="扣款金额(元)" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.deductAmount" :min="0" :precision="2" size="small" class="w-full" :disabled="isFinanceAudit" />
          </template>
        </el-table-column>
        <el-table-column label="照片" width="100">
          <template #default="{ row }">
            <el-upload
              :action="uploadUrl"
              :on-success="(res, file) => handleUploadSuccess(row, res, file)"
              :file-list="row.photoFileList"
              list-type="picture-card"
              :limit="3"
              :disabled="isFinanceAudit"
              class="w-full"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </template>
        </el-table-column>
        <el-table-column label="责任说明" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.responsibilityNote" type="textarea" :rows="2" placeholder="请填写责任说明" size="small" :disabled="isFinanceAudit" />
          </template>
        </el-table-column>
      </el-table>

      <div class="mb-4 p-4 bg-gray-50 rounded-lg">
        <div class="flex justify-between items-center text-lg">
          <div>
            <span>扣款总额：</span>
            <span class="text-red-500 font-bold text-xl">-¥{{ totalDeductAmount.toFixed(2) }}</span>
          </div>
          <div>
            <span>应退金额：</span>
            <span class="text-green-600 font-bold text-xl">¥{{ refundAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <el-form v-if="!isFinanceAudit" label-width="120px">
        <el-form-item label="检查备注">
          <el-input v-model="checkForm.checkRemark" type="textarea" :rows="3" placeholder="请输入检查备注" class="rounded-lg" />
        </el-form-item>
      </el-form>

      <el-form v-if="isFinanceAudit" label-width="120px">
        <el-form-item label="财务备注">
          <el-input v-model="checkForm.financeRemark" type="textarea" :rows="3" placeholder="请输入财务审核备注" class="rounded-lg" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="checkDialogVisible = false" class="rounded-lg">取消</el-button>
          <el-button type="primary" @click="handleCheckSubmit" class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200" :loading="checking">{{ checkDialogTitle }}</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="refundDialogVisible" title="确认退款" width="500px" class="rounded-lg">
      <el-form :model="refundForm" label-width="120px">
        <el-form-item label="应退金额">
          <span class="text-green-600 font-bold text-xl">¥{{ currentApplication?.refundAmount || '0.00' }}</span>
        </el-form-item>
        <el-form-item label="实退金额" prop="actualRefundAmount">
          <el-input-number v-model="refundForm.actualRefundAmount" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item label="退款凭证">
          <el-input v-model="refundForm.refundVoucher" placeholder="请输入退款凭证号" class="rounded-lg" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="refundDialogVisible = false" class="rounded-lg">取消</el-button>
          <el-button type="primary" @click="handleRefundSubmit" class="rounded-lg" :loading="refunding">确认退款</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="驳回申请" width="500px" class="rounded-lg">
      <el-form label-width="100px">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入驳回原因" class="rounded-lg" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="rejectDialogVisible = false" class="rounded-lg">取消</el-button>
          <el-button type="danger" @click="handleRejectSubmit" class="rounded-lg" :loading="rejecting">确认驳回</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="申请详情" width="900px" class="rounded-lg">
      <el-descriptions :column="2" border class="mb-4">
        <el-descriptions-item label="申请单号">{{ detailData?.application?.applicationNo }}</el-descriptions-item>
        <el-descriptions-item label="工人姓名">{{ detailData?.application?.workerName }}</el-descriptions-item>
        <el-descriptions-item label="离职类型">{{ detailData?.application?.resignationType === 'resign' ? '完全离职' : '企业内转项目' }}</el-descriptions-item>
        <el-descriptions-item label="当前项目">{{ detailData?.application?.currentProjectName }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData?.application?.targetProjectName" label="目标项目">{{ detailData?.application?.targetProjectName }}</el-descriptions-item>
        <el-descriptions-item label="最后工作日">{{ detailData?.application?.lastWorkDate }}</el-descriptions-item>
        <el-descriptions-item label="押金总额">¥{{ detailData?.application?.totalDeposit }}</el-descriptions-item>
        <el-descriptions-item label="扣款总额"><span class="text-red-500">-¥{{ detailData?.application?.deductAmount }}</span></el-descriptions-item>
        <el-descriptions-item label="应退金额"><span class="text-green-600">¥{{ detailData?.application?.refundAmount }}</span></el-descriptions-item>
        <el-descriptions-item label="实退金额"><span class="text-green-600">¥{{ detailData?.application?.actualRefundAmount || '0.00' }}</span></el-descriptions-item>
        <el-descriptions-item label="申请状态">
          <el-tag :type="getStatusType(detailData?.application?.applicationStatus)" size="small">{{ getStatusText(detailData?.application?.applicationStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detailData?.application?.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData?.application?.teamLeaderName" label="班组长">{{ detailData?.application?.teamLeaderName }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData?.application?.checkTime" label="检查时间">{{ detailData?.application?.checkTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData?.application?.financeUserName" label="财务审核人">{{ detailData?.application?.financeUserName }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData?.application?.refundTime" label="退款时间">{{ detailData?.application?.refundTime }}</el-descriptions-item>
      </el-descriptions>

      <div class="mb-4">
        <h4 class="font-bold mb-2">装备归还明细</h4>
        <el-table :data="detailData?.equipmentReturns || []" border size="small">
          <el-table-column prop="equipmentName" label="装备名称" />
          <el-table-column prop="equipmentType" label="类型" width="100">
            <template #default="{ row }">{{ getEquipmentTypeText(row.equipmentType) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="depositAmount" label="押金(元)" width="100" />
          <el-table-column prop="returnStatus" label="归还状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getReturnStatusType(row.returnStatus)" size="small">{{ getReturnStatusText(row.returnStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="returnCondition" label="归还状况" width="100">
            <template #default="{ row }">{{ getReturnConditionText(row.returnCondition) }}</template>
          </el-table-column>
          <el-table-column prop="deductAmount" label="扣款(元)" width="100">
            <template #default="{ row }"><span class="text-red-500">-¥{{ row.deductAmount || '0.00' }}</span></template>
          </el-table-column>
          <el-table-column prop="responsibilityNote" label="责任说明" min-width="150" />
        </el-table>
      </div>

      <el-descriptions v-if="detailData?.application?.checkRemark" :column="1" border class="mb-4">
        <el-descriptions-item label="检查备注">{{ detailData?.application?.checkRemark }}</el-descriptions-item>
      </el-descriptions>
      <el-descriptions v-if="detailData?.application?.financeRemark" :column="1" border class="mb-4">
        <el-descriptions-item label="财务备注">{{ detailData?.application?.financeRemark }}</el-descriptions-item>
      </el-descriptions>
      <el-descriptions v-if="detailData?.application?.responsibilityNote" :column="1" border>
        <el-descriptions-item label="责任说明">{{ detailData?.application?.responsibilityNote }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <div class="flex justify-end">
          <el-button @click="detailDialogVisible = false" class="rounded-lg">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { resignationApi } from '@/api/resignation'
import { workerApi } from '@/api/worker'
import { enterpriseApi } from '@/api/enterprise'
import { projectApi } from '@/api/project'
import { workerEquipmentApi } from '@/api/workerEquipment'
import { useRoute } from 'vue-router'

const route = useRoute()
const uploadUrl = import.meta.env.VITE_API_BASE_URL + '/file/upload'

const loading = ref(false)
const creating = ref(false)
const checking = ref(false)
const refunding = ref(false)
const rejecting = ref(false)
const tableData = ref([])
const createDialogVisible = ref(false)
const checkDialogVisible = ref(false)
const refundDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const createFormRef = ref(null)
const selectedWorker = ref(null)
const currentApplication = ref(null)
const currentApplicationId = ref(null)
const workerList = ref([])
const enterpriseList = ref([])
const projectList = ref([])
const workerEquipmentList = ref([])
const checkEquipmentList = ref([])
const detailData = ref(null)
const checkDialogTitle = ref('班组长检查')
const isFinanceAudit = ref(false)
const rejectReason = ref('')

const searchForm = reactive({
  workerName: '',
  enterpriseId: null,
  projectId: null,
  applicationStatus: '',
  resignationType: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const createForm = reactive({
  workerId: null,
  resignationType: 'resign',
  targetProjectId: null,
  lastWorkDate: null,
  responsibilityNote: ''
})

const createRules = {
  workerId: [{ required: true, message: '请选择工人', trigger: 'change' }],
  resignationType: [{ required: true, message: '请选择离职类型', trigger: 'change' }],
  lastWorkDate: [{ required: true, message: '请选择最后工作日', trigger: 'change' }]
}

const checkForm = reactive({
  checkRemark: '',
  financeRemark: ''
})

const refundForm = reactive({
  actualRefundAmount: 0,
  refundVoucher: ''
})

const sameEnterpriseProjects = computed(() => {
  if (!selectedWorker.value) return []
  return projectList.value.filter(p => p.enterpriseId === selectedWorker.value.enterpriseId && p.id !== selectedWorker.value.projectId)
})

const totalDeductAmount = computed(() => {
  return checkEquipmentList.value.reduce((sum, item) => sum + (item.deductAmount || 0), 0)
})

const refundAmount = computed(() => {
  return (currentApplication.value?.totalDeposit || 0) - totalDeductAmount.value
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await resignationApi.page({
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadEnterpriseList = async () => {
  try {
    const res = await enterpriseApi.list()
    if (res.code === 200) {
      enterpriseList.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const loadProjectList = async () => {
  try {
    const res = await projectApi.list()
    if (res.code === 200) {
      projectList.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const loadWorkerList = async () => {
  try {
    const res = await workerApi.page({ current: 1, size: 1000, workerStatus: 'on_job' })
    if (res.code === 200) {
      workerList.value = res.data.records
    }
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    workerName: '',
    enterpriseId: null,
    projectId: null,
    applicationStatus: '',
    resignationType: ''
  })
  handleSearch()
}

const handleAdd = () => {
  Object.assign(createForm, {
    workerId: route.query.workerId ? Number(route.query.workerId) : null,
    resignationType: 'resign',
    targetProjectId: null,
    lastWorkDate: null,
    responsibilityNote: ''
  })
  selectedWorker.value = null
  workerEquipmentList.value = []
  createDialogVisible.value = true
  if (createForm.workerId) {
    handleWorkerChange(createForm.workerId)
  }
  if (createFormRef.value) createFormRef.value.clearValidate()
}

const handleWorkerChange = async (workerId) => {
  if (!workerId) {
    selectedWorker.value = null
    workerEquipmentList.value = []
    return
  }
  try {
    const workerRes = await workerApi.getById(workerId)
    if (workerRes.code === 200) {
      selectedWorker.value = workerRes.data
    }
    const equipRes = await workerEquipmentApi.getActiveEquipment(workerId)
    if (equipRes.code === 200) {
      workerEquipmentList.value = equipRes.data
      if (equipRes.data.length === 0) {
        ElMessage.warning('该工人没有在用装备，无需走归还流程')
      }
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取工人信息失败')
  }
}

const handleCreateSubmit = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      if (workerEquipmentList.value.length === 0) {
        ElMessage.warning('该工人没有在用装备，无需走归还流程')
        return
      }
      creating.value = true
      try {
        const res = await resignationApi.create(createForm, { userId: 1, userName: '管理员' })
        if (res.code === 200) {
          ElMessage.success('申请创建成功')
          createDialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
        ElMessage.error(error.message || '创建失败')
      } finally {
        creating.value = false
      }
    }
  })
}

const handleView = async (row) => {
  try {
    const res = await resignationApi.getDetail(row.id)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取详情失败')
  }
}

const handleTeamLeaderCheck = async (row) => {
  isFinanceAudit.value = false
  checkDialogTitle.value = '班组长检查'
  await openCheckDialog(row)
}

const handleFinanceAudit = async (row) => {
  isFinanceAudit.value = true
  checkDialogTitle.value = '财务审核'
  await openCheckDialog(row)
}

const openCheckDialog = async (row) => {
  currentApplicationId.value = row.id
  try {
    const res = await resignationApi.getDetail(row.id)
    if (res.code === 200) {
      currentApplication.value = res.data.application
      checkEquipmentList.value = res.data.equipmentReturns.map(item => ({
        ...item,
        photoFileList: item.photoIds ? item.photoIds.split(',').map(id => ({ id, name: '照片', url: `/file/${id}` })) : []
      }))
      checkForm.checkRemark = res.data.application.checkRemark || ''
      checkForm.financeRemark = res.data.application.financeRemark || ''
      checkDialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取详情失败')
  }
}

const handleUploadSuccess = (row, res) => {
  if (res.code === 200) {
    if (!row.photoIds) {
      row.photoIds = String(res.data.id)
    } else {
      row.photoIds += ',' + res.data.id
    }
  }
}

const handleCheckSubmit = async () => {
  const hasIssues = checkEquipmentList.value.some(item =>
    (item.returnStatus === 'lost' || item.returnStatus === 'damaged') &&
    (!item.photoIds || !item.responsibilityNote)
  )

  if (hasIssues && !isFinanceAudit.value) {
    ElMessage.warning('缺失或损坏的装备必须上传照片并填写责任说明')
    return
  }

  checking.value = true
  try {
    const submitData = {
      equipmentReturns: checkEquipmentList.value.map(item => ({
        id: item.id,
        workerEquipmentId: item.workerEquipmentId,
        returnStatus: item.returnStatus,
        returnCondition: item.returnCondition,
        deductAmount: item.deductAmount,
        photoIds: item.photoIds,
        responsibilityNote: item.responsibilityNote
      })),
      checkRemark: checkForm.checkRemark,
      financeRemark: checkForm.financeRemark
    }

    let res
    if (isFinanceAudit.value) {
      res = await resignationApi.financeAudit(currentApplicationId.value, submitData, { userId: 1, userName: '管理员' })
    } else {
      res = await resignationApi.teamLeaderCheck(currentApplicationId.value, submitData, { userId: 1, userName: '管理员' })
    }

    if (res.code === 200) {
      ElMessage.success(isFinanceAudit.value ? '审核完成' : '检查完成')
      checkDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || (isFinanceAudit.value ? '审核失败' : '检查失败'))
  } finally {
    checking.value = false
  }
}

const handleConfirmRefund = (row) => {
  currentApplication.value = row
  refundForm.actualRefundAmount = row.refundAmount
  refundForm.refundVoucher = ''
  refundDialogVisible.value = true
}

const handleRefundSubmit = async () => {
  refunding.value = true
  try {
    const res = await resignationApi.confirmRefund(currentApplication.value.id, {
      actualRefundAmount: refundForm.actualRefundAmount,
      refundVoucher: refundForm.refundVoucher,
      userId: 1,
      userName: '管理员'
    })
    if (res.code === 200) {
      ElMessage.success('退款确认完成')
      refundDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || '确认失败')
  } finally {
    refunding.value = false
  }
}

const handleReject = (row) => {
  currentApplicationId.value = row.id
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const handleRejectSubmit = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  rejecting.value = true
  try {
    const res = await resignationApi.reject(currentApplicationId.value, {
      rejectReason: rejectReason.value,
      userId: 1,
      userName: '管理员'
    })
    if (res.code === 200) {
      ElMessage.success('驳回成功')
      rejectDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || '驳回失败')
  } finally {
    rejecting.value = false
  }
}

const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    checked: 'success',
    pending_finance: 'warning',
    refunded: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待班组长检查',
    checked: '班组长已检查',
    pending_finance: '待财务审核',
    refunded: '已退款',
    rejected: '已驳回'
  }
  return texts[status] || status
}

const getEquipmentTypeText = (type) => {
  const texts = { badge: '工牌', helmet: '安全帽', uniform: '工服', toolkit: '工具包', other: '其他' }
  return texts[type] || type
}

const getReturnStatusType = (status) => {
  const types = { returned: 'success', retained: 'warning', lost: 'danger', damaged: 'danger' }
  return types[status] || 'info'
}

const getReturnStatusText = (status) => {
  const texts = { returned: '已归还', retained: '转项目保留', lost: '遗失', damaged: '损坏' }
  return texts[status] || status
}

const getReturnConditionText = (condition) => {
  const texts = { good: '良好', damaged: '损坏', serious_damage: '严重损坏' }
  return texts[condition] || '-'
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

onMounted(() => {
  loadData()
  loadEnterpriseList()
  loadProjectList()
  loadWorkerList()
})
</script>

<style scoped>
.resignation-page { max-width: 1500px; margin: 0 auto; }
:deep(.el-card) { border-radius: 12px; }
:deep(.el-table) { border-radius: 8px; }
:deep(.el-button) { border-radius: 6px; }
:deep(.el-input__wrapper) { border-radius: 6px; }
:deep(.el-select .el-input__wrapper) { border-radius: 6px; }
:deep(.el-dialog) { border-radius: 12px; }
</style>
