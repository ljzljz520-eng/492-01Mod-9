<template>
  <div class="worker-page">
    <van-search
      v-model="searchKeyword"
      placeholder="搜索工人姓名/手机号"
      @search="fetchWorkers"
    />
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="fetchWorkers"
      >
        <van-cell-group inset class="mt-3">
          <van-cell
            v-for="worker in workerList"
            :key="worker.id"
            :title="worker.workerName"
            :label="worker.phone"
            is-link
            @click="showWorkerDetail(worker)"
          >
            <template #right-icon>
              <van-tag :type="getStatusType(worker.workerStatus)">
                {{ getStatusText(worker.workerStatus) }}
              </van-tag>
            </template>
          </van-cell>
        </van-cell-group>
      </van-list>
    </van-pull-refresh>

    <van-popup v-model:show="showDetail" round position="bottom" :style="{ height: '70%' }">
      <div class="p-4">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-bold">{{ currentWorker?.workerName }}</h3>
          <van-button size="small" type="primary" @click="showEquipmentList">
            查看装备
          </van-button>
        </div>
        <van-cell-group inset>
          <van-cell title="手机号" :value="currentWorker?.phone" />
          <van-cell title="身份证号" :value="currentWorker?.idCard" />
          <van-cell title="所属项目" :value="currentWorker?.projectName" />
          <van-cell title="入职日期" :value="currentWorker?.hireDate" />
        </van-cell-group>
        <div class="mt-4 flex gap-3">
          <van-button type="danger" block @click="createResignation">
            发起离职申请
          </van-button>
        </div>
      </div>
    </van-popup>

    <van-popup v-model:show="showEquipment" round position="bottom" :style="{ height: '60%' }">
      <div class="p-4">
        <h3 class="text-lg font-bold mb-4">领用装备列表</h3>
        <van-cell-group inset v-if="equipmentList.length > 0">
          <van-cell
            v-for="item in equipmentList"
            :key="item.id"
            :title="item.equipmentName"
            :label="item.equipmentCode"
          >
            <template #right-icon>
              <van-tag :type="item.status === 'in_use' ? 'primary' : 'success'">
                {{ item.status === 'in_use' ? '使用中' : '已归还' }}
              </van-tag>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-else description="暂无领用装备" />
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { workerApi } from '@/api/worker'
import { workerEquipmentApi } from '@/api/workerEquipment'
import { resignationApi } from '@/api/resignation'

const router = useRouter()
const searchKeyword = ref('')
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const workerList = ref([])
const equipmentList = ref([])
const showDetail = ref(false)
const showEquipment = ref(false)
const currentWorker = ref(null)

const fetchWorkers = async () => {
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value
    }
    const res = await workerApi.page(params)
    if (res.code === 200) {
      if (refreshing.value) {
        workerList.value = []
        refreshing.value = false
      }
      workerList.value = [...workerList.value, ...(res.data?.records || [])]
      loading.value = false
      if (workerList.value.length >= (res.data?.total || 0)) {
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
  fetchWorkers()
}

const getStatusType = (status) => {
  const types = {
    on_job: 'primary',
    off_job: 'warning',
    resigned: 'danger',
    transferred: 'success'
  }
  return types[status] || 'default'
}

const getStatusText = (status) => {
  const texts = {
    on_job: '在职',
    off_job: '离岗',
    resigned: '已离职',
    transferred: '已转岗'
  }
  return texts[status] || status
}

const showWorkerDetail = (worker) => {
  currentWorker.value = worker
  showDetail.value = true
}

const showEquipmentList = async () => {
  try {
    const res = await workerEquipmentApi.getByWorkerId(currentWorker.value.id)
    if (res.code === 200) {
      equipmentList.value = res.data || []
      showEquipment.value = true
    }
  } catch (e) {
    showToast('获取装备列表失败')
  }
}

const createResignation = async () => {
  try {
    await showConfirmDialog({
      title: '确认发起',
      message: `确定要为 ${currentWorker.value.workerName} 发起离职申请吗？`
    })
    const dto = {
      workerId: currentWorker.value.id,
      resignationType: 'resign'
    }
    const res = await resignationApi.create(dto)
    if (res.code === 200) {
      showToast('申请已提交')
      showDetail.value = false
      router.push('/h5/resignation')
    }
  } catch (e) {
    if (e !== 'cancel') {
      showToast(e.message || '操作失败')
    }
  }
}

onMounted(() => {
  fetchWorkers()
})
</script>

<style scoped>
.worker-page {
  padding-bottom: 20px;
}
.mt-3 {
  margin-top: 12px;
}
.mt-4 {
  margin-top: 16px;
}
.p-4 {
  padding: 16px;
}
</style>
