<template>
  <div class="equipment-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">装备管理</span>
          <el-button type="primary" @click="handleAdd" class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200">
            <el-icon><Plus /></el-icon>
            新增装备
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="装备名称">
          <el-input v-model="searchForm.equipmentName" placeholder="请输入装备名称" clearable class="rounded-lg" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="装备类型">
          <el-select v-model="searchForm.equipmentType" placeholder="请选择类型" clearable style="width: 180px" class="rounded-lg">
            <el-option label="工牌" value="badge" />
            <el-option label="安全帽" value="helmet" />
            <el-option label="工服" value="uniform" />
            <el-option label="工具包" value="toolkit" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200">查询</el-button>
          <el-button @click="handleReset" class="rounded-lg">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border class="rounded-lg overflow-hidden" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="equipmentName" label="装备名称" min-width="150" />
        <el-table-column prop="equipmentType" label="装备类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.equipmentType)" size="small">{{ getTypeText(row.equipmentType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="equipmentCode" label="装备编号" width="150" />
        <el-table-column prop="specification" label="规格型号" min-width="150" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="depositAmount" label="押金(元)" width="120">
          <template #default="{ row }">
            <span class="text-red-500 font-medium">¥{{ row.depositAmount || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalQuantity" label="总数量" width="100" />
        <el-table-column prop="availableQuantity" label="可用数量" width="100">
          <template #default="{ row }">
            <el-tag :type="row.availableQuantity > 0 ? 'success' : 'danger'" size="small">{{ row.availableQuantity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-6 flex justify-end">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" class="rounded-lg" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" class="rounded-lg" :close-on-click-modal="false">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px" class="px-2">
        <el-form-item label="装备名称" prop="equipmentName">
          <el-input v-model="formData.equipmentName" placeholder="请输入装备名称" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="装备类型" prop="equipmentType">
          <el-select v-model="formData.equipmentType" placeholder="请选择装备类型" class="w-full rounded-lg">
            <el-option label="工牌" value="badge" />
            <el-option label="安全帽" value="helmet" />
            <el-option label="工服" value="uniform" />
            <el-option label="工具包" value="toolkit" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="装备编号" prop="equipmentCode">
          <el-input v-model="formData.equipmentCode" placeholder="请输入装备编号" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="formData.specification" placeholder="请输入规格型号" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="formData.unit" placeholder="请输入单位" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="押金金额" prop="depositAmount">
          <el-input-number v-model.number="formData.depositAmount" :min="0" :precision="2" class="w-full rounded-lg" />
        </el-form-item>
        <el-form-item label="总数量" prop="totalQuantity">
          <el-input-number v-model.number="formData.totalQuantity" :min="0" class="w-full rounded-lg" @change="handleTotalQuantityChange" />
        </el-form-item>
        <el-form-item label="可用数量" prop="availableQuantity">
          <el-input-number v-model.number="formData.availableQuantity" :min="0" :max="formData.totalQuantity" class="w-full rounded-lg" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" class="rounded-lg" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false" class="rounded-lg">取消</el-button>
          <el-button type="primary" @click="handleSubmit" class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { equipmentApi } from '@/api/equipment'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增装备')
const formRef = ref(null)

const searchForm = reactive({
  equipmentName: '',
  equipmentType: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  equipmentName: '',
  equipmentType: '',
  equipmentCode: '',
  specification: '',
  unit: '件',
  depositAmount: 0,
  totalQuantity: 0,
  availableQuantity: 0,
  remark: ''
})

const formRules = {
  equipmentName: [{ required: true, message: '请输入装备名称', trigger: 'blur' }],
  equipmentType: [{ required: true, message: '请选择装备类型', trigger: 'change' }],
  equipmentCode: [{ required: true, message: '请输入装备编号', trigger: 'blur' }],
  depositAmount: [{ required: true, message: '请输入押金金额', trigger: 'blur' }],
  totalQuantity: [{ required: true, message: '请输入总数量', trigger: 'blur' }],
  availableQuantity: [{ required: true, message: '请输入可用数量', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await equipmentApi.page({
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

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    equipmentName: '',
    equipmentType: ''
  })
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增装备'
  Object.assign(formData, {
    id: null,
    equipmentName: '',
    equipmentType: '',
    equipmentCode: '',
    specification: '',
    unit: '件',
    depositAmount: 0,
    totalQuantity: 0,
    availableQuantity: 0,
    remark: ''
  })
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑装备'
  try {
    const res = await equipmentApi.getById(row.id)
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
          res = await equipmentApi.update(formData.id, formData)
        } else {
          res = await equipmentApi.save(formData)
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
    await ElMessageBox.confirm('确定要删除该装备吗？', '提示', { type: 'warning', confirmButtonClass: 'el-button--danger' })
    const res = await equipmentApi.delete(row.id)
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

const handleTotalQuantityChange = (val) => {
  if (formData.availableQuantity > val) {
    formData.availableQuantity = val
  }
}

const getTypeText = (type) => {
  const texts = { badge: '工牌', helmet: '安全帽', uniform: '工服', toolkit: '工具包', other: '其他' }
  return texts[type] || type
}

const getTypeTagType = (type) => {
  const types = { badge: '', helmet: 'warning', uniform: 'primary', toolkit: 'success', other: 'info' }
  return types[type] || ''
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
})
</script>

<style scoped>
.equipment-page { max-width: 1400px; margin: 0 auto; }
:deep(.el-card) { border-radius: 12px; }
:deep(.el-table) { border-radius: 8px; }
:deep(.el-button) { border-radius: 6px; }
:deep(.el-input__wrapper) { border-radius: 6px; }
:deep(.el-select .el-input__wrapper) { border-radius: 6px; }
:deep(.el-dialog) { border-radius: 12px; }
</style>
