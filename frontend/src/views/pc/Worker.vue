<template>
  <div class="worker-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">工人管理</span>
          <el-button type="primary" @click="handleAdd" class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200">
            <el-icon><Plus /></el-icon>
            新增工人
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="工人姓名">
          <el-input v-model="searchForm.workerName" placeholder="请输入工人姓名" clearable class="rounded-lg" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable class="rounded-lg" @keyup.enter="handleSearch" />
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
        <el-form-item label="工人状态">
          <el-select v-model="searchForm.workerStatus" placeholder="请选择状态" clearable style="width: 150px" class="rounded-lg">
            <el-option label="在岗" value="on_job" />
            <el-option label="待离职" value="off_job" />
            <el-option label="已离职" value="resigned" />
            <el-option label="已转岗" value="transferred" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200">查询</el-button>
          <el-button @click="handleReset" class="rounded-lg">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border class="rounded-lg overflow-hidden" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="workerName" label="姓名" width="100" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">{{ row.gender === 'male' ? '男' : row.gender === 'female' ? '女' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="depositAmount" label="押金(元)" width="100">
          <template #default="{ row }">¥{{ row.depositAmount || '0.00' }}</template>
        </el-table-column>
        <el-table-column prop="depositPaid" label="押金状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.depositPaid === 1 ? 'success' : 'warning'" size="small">{{ row.depositPaid === 1 ? '已缴' : '未缴' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workerStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.workerStatus)" size="small">{{ getStatusText(row.workerStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" size="small" link @click="handleEquipment(row)">装备管理</el-button>
            <el-button type="warning" size="small" link @click="handleResignation(row)">离职申请</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-6 flex justify-end">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" class="rounded-lg" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" class="rounded-lg" :close-on-click-modal="false">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px" class="px-2">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="workerName"><el-input v-model="formData.workerName" placeholder="请输入姓名" class="rounded-lg" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard"><el-input v-model="formData.idCard" placeholder="请输入身份证号" class="rounded-lg" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone"><el-input v-model="formData.phone" placeholder="请输入手机号" class="rounded-lg" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="formData.gender" placeholder="请选择性别" class="w-full rounded-lg">
                <el-option label="男" value="male" />
                <el-option label="女" value="female" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属企业" prop="enterpriseId">
              <el-select v-model="formData.enterpriseId" placeholder="请选择企业" class="w-full rounded-lg" @change="handleEnterpriseChange">
                <el-option v-for="item in enterpriseList" :key="item.id" :label="item.enterpriseName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属项目" prop="projectId">
              <el-select v-model="formData.projectId" placeholder="请选择项目" class="w-full rounded-lg">
                <el-option v-for="item in filteredProjects" :key="item.id" :label="item.projectName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期"><el-date-picker v-model="formData.hireDate" type="date" placeholder="请选择入职日期" class="w-full rounded-lg" value-format="YYYY-MM-DD" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="押金金额" prop="depositAmount"><el-input v-model.number="formData.depositAmount" type="number" placeholder="请输入押金金额" class="rounded-lg" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="押金已缴">
              <el-switch v-model="formData.depositPaid" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工人状态" prop="workerStatus">
              <el-select v-model="formData.workerStatus" placeholder="请选择状态" class="w-full rounded-lg">
                <el-option label="在岗" value="on_job" />
                <el-option label="待离职" value="off_job" />
                <el-option label="已离职" value="resigned" />
                <el-option label="已转岗" value="transferred" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="家庭住址"><el-input v-model="formData.address" placeholder="请输入家庭住址" class="rounded-lg" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急联系人"><el-input v-model="formData.emergencyContact" placeholder="请输入紧急联系人" class="rounded-lg" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急联系电话"><el-input v-model="formData.emergencyPhone" placeholder="请输入紧急联系电话" class="rounded-lg" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注"><el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" class="rounded-lg" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false" class="rounded-lg">取消</el-button>
          <el-button type="primary" @click="handleSubmit" class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="equipmentDialogVisible" title="工人装备管理" width="900px" class="rounded-lg">
      <div class="mb-4 flex justify-between items-center">
        <div class="text-gray-700">
          工人：<span class="font-bold">{{ selectedWorker?.workerName }}</span>
          &nbsp;&nbsp;
          押金总额：<span class="text-red-500 font-bold">¥{{ totalDeposit || '0.00' }}</span>
        </div>
        <el-button type="primary" size="small" @click="showDistributeDialog" class="rounded-lg">
          <el-icon><Plus /></el-icon>
          发放装备
        </el-button>
      </div>
      <el-table :data="equipmentList" v-loading="equipmentLoading" border class="rounded-lg overflow-hidden">
        <el-table-column prop="equipmentName" label="装备名称" width="150" />
        <el-table-column prop="equipmentType" label="装备类型" width="120">
          <template #default="{ row }">{{ getEquipmentTypeText(row.equipmentType) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="depositAmount" label="押金(元)" width="100">
          <template #default="{ row }">¥{{ row.depositAmount }}</template>
        </el-table-column>
        <el-table-column prop="receiveDate" label="领用日期" width="120" />
        <el-table-column prop="receiveUserName" label="发放人" width="100" />
        <el-table-column prop="equipmentStatus" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getEquipmentStatusType(row.equipmentStatus)" size="small">{{ getEquipmentStatusText(row.equipmentStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.equipmentStatus === 'in_use' || row.equipmentStatus === 'retained'" type="success" size="small" link @click="handleReturnEquipment(row)">归还</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="flex justify-end">
          <el-button @click="equipmentDialogVisible = false" class="rounded-lg">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="distributeDialogVisible" title="发放装备" width="500px" class="rounded-lg">
      <el-form :model="distributeForm" label-width="100px">
        <el-form-item label="选择装备" prop="equipmentId">
          <el-select v-model="distributeForm.equipmentId" placeholder="请选择装备" class="w-full rounded-lg">
            <el-option v-for="item in availableEquipments" :key="item.id" :label="`${item.equipmentName} (库存:${item.availableQuantity}, 押金:¥${item.depositAmount})`" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="distributeDialogVisible = false" class="rounded-lg">取消</el-button>
          <el-button type="primary" @click="handleDistribute" class="rounded-lg" :loading="distributing">确定发放</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { workerApi } from '@/api/worker'
import { enterpriseApi } from '@/api/enterprise'
import { projectApi } from '@/api/project'
import { equipmentApi } from '@/api/equipment'
import { workerEquipmentApi } from '@/api/workerEquipment'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const equipmentLoading = ref(false)
const distributing = ref(false)
const tableData = ref([])
const equipmentList = ref([])
const dialogVisible = ref(false)
const equipmentDialogVisible = ref(false)
const distributeDialogVisible = ref(false)
const dialogTitle = ref('新增工人')
const formRef = ref(null)
const selectedWorker = ref(null)
const enterpriseList = ref([])
const projectList = ref([])
const availableEquipments = ref([])

const searchForm = reactive({
  workerName: '',
  phone: '',
  enterpriseId: null,
  projectId: null,
  workerStatus: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  workerName: '',
  idCard: '',
  phone: '',
  gender: '',
  birthDate: null,
  address: '',
  emergencyContact: '',
  emergencyPhone: '',
  enterpriseId: null,
  projectId: null,
  workerStatus: 'on_job',
  hireDate: null,
  depositAmount: 0,
  depositPaid: 0,
  remark: ''
})

const distributeForm = reactive({
  equipmentId: null
})

const formRules = {
  workerName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  enterpriseId: [{ required: true, message: '请选择企业', trigger: 'change' }],
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }]
}

const filteredProjects = computed(() => {
  if (!formData.enterpriseId) return projectList.value
  return projectList.value.filter(p => p.enterpriseId === formData.enterpriseId)
})

const totalDeposit = computed(() => {
  return equipmentList.value
    .filter(e => e.equipmentStatus === 'in_use' || e.equipmentStatus === 'retained')
    .reduce((sum, e) => sum + (e.depositAmount * e.quantity || 0), 0)
    .toFixed(2)
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await workerApi.page({
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

const loadAvailableEquipments = async () => {
  try {
    const res = await equipmentApi.list()
    if (res.code === 200) {
      availableEquipments.value = res.data.filter(e => e.availableQuantity > 0)
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
    phone: '',
    enterpriseId: null,
    projectId: null,
    workerStatus: ''
  })
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增工人'
  Object.assign(formData, {
    id: null,
    workerName: '',
    idCard: '',
    phone: '',
    gender: '',
    birthDate: null,
    address: '',
    emergencyContact: '',
    emergencyPhone: '',
    enterpriseId: null,
    projectId: null,
    workerStatus: 'on_job',
    hireDate: null,
    depositAmount: 0,
    depositPaid: 0,
    remark: ''
  })
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑工人'
  try {
    const res = await workerApi.getById(row.id)
    if (res.code === 200) {
      Object.assign(formData, res.data)
      dialogVisible.value = true
      if (formRef.value) formRef.value.clearValidate()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取数据失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        let res
        if (formData.id) {
          res = await workerApi.update(formData.id, formData)
        } else {
          res = await workerApi.save(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.id ? '更新成功' : '新增成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
        ElMessage.error(formData.id ? '更新失败' : '新增失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该工人吗？', '提示', { type: 'warning', confirmButtonClass: 'el-button--danger' })
    const res = await workerApi.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

const handleEquipment = async (row) => {
  selectedWorker.value = row
  equipmentDialogVisible.value = true
  equipmentLoading.value = true
  try {
    const res = await workerEquipmentApi.getByWorkerId(row.id)
    if (res.code === 200) {
      equipmentList.value = res.data
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载装备数据失败')
  } finally {
    equipmentLoading.value = false
  }
}

const handleReturnEquipment = async (row) => {
  try {
    await ElMessageBox.confirm('确定要归还该装备吗？', '提示', { type: 'warning' })
    const res = await workerEquipmentApi.returnEquipment(row.id)
    if (res.code === 200) {
      ElMessage.success('归还成功')
      handleEquipment(selectedWorker.value)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('归还失败')
    }
  }
}

const showDistributeDialog = async () => {
  await loadAvailableEquipments()
  distributeForm.equipmentId = null
  distributeDialogVisible.value = true
}

const handleDistribute = async () => {
  if (!distributeForm.equipmentId) {
    ElMessage.warning('请选择装备')
    return
  }
  distributing.value = true
  try {
    const res = await workerEquipmentApi.distribute({
      workerId: selectedWorker.value.id,
      equipmentId: distributeForm.equipmentId,
      userId: 1,
      userName: '管理员'
    })
    if (res.code === 200) {
      ElMessage.success('发放成功')
      distributeDialogVisible.value = false
      handleEquipment(selectedWorker.value)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('发放失败')
  } finally {
    distributing.value = false
  }
}

const handleResignation = (row) => {
  router.push({ path: '/pc/resignation', query: { workerId: row.id } })
}

const handleEnterpriseChange = () => {
  formData.projectId = null
}

const getStatusType = (status) => {
  const types = { on_job: 'success', off_job: 'warning', resigned: 'info', transferred: 'primary' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { on_job: '在岗', off_job: '待离职', resigned: '已离职', transferred: '已转岗' }
  return texts[status] || status
}

const getEquipmentTypeText = (type) => {
  const texts = { badge: '工牌', helmet: '安全帽', uniform: '工服', toolkit: '工具包', other: '其他' }
  return texts[type] || type
}

const getEquipmentStatusType = (status) => {
  const types = { in_use: 'primary', returned: 'success', retained: 'warning', lost: 'danger', damaged: 'danger' }
  return types[status] || 'info'
}

const getEquipmentStatusText = (status) => {
  const texts = { in_use: '使用中', returned: '已归还', retained: '转项目保留', lost: '遗失', damaged: '损坏' }
  return texts[status] || status
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
})
</script>

<style scoped>
.worker-page { max-width: 1400px; margin: 0 auto; }
:deep(.el-card) { border-radius: 12px; }
:deep(.el-table) { border-radius: 8px; }
:deep(.el-button) { border-radius: 6px; }
:deep(.el-input__wrapper) { border-radius: 6px; }
:deep(.el-select .el-input__wrapper) { border-radius: 6px; }
:deep(.el-dialog) { border-radius: 12px; }
</style>
