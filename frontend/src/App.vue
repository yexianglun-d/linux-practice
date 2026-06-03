<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getCatalog } from './api'
import AdminPanel from './components/AdminPanel.vue'
import TerminalFirstApp from './components/TerminalFirstApp.vue'
import type { CatalogResponse } from './types'

const isAdminRoute = ref(window.location.pathname === '/admin')
const adminCatalog = ref<CatalogResponse | null>(null)

onMounted(() => {
  window.addEventListener('popstate', syncRoute)
  if (isAdminRoute.value) {
    loadAdminCatalog()
  }
})

function syncRoute() {
  isAdminRoute.value = window.location.pathname === '/admin'
  if (isAdminRoute.value) {
    loadAdminCatalog()
  }
}

async function loadAdminCatalog() {
  adminCatalog.value = await getCatalog()
}
</script>

<template>
  <main v-if="!isAdminRoute" class="root-terminal">
    <TerminalFirstApp />
  </main>
  <main v-else class="admin-route">
    <AdminPanel :catalog="adminCatalog" @refresh-catalog="loadAdminCatalog" />
  </main>
</template>
