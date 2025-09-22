<script setup>
import { ref, onMounted, } from 'vue';
import axios from 'axios';

</script>

<template>
  <div class = "container">
    <div class = "header">
        <h2>구매 목록(공급처)</h2>
    </div>
  </div>
</template>

<style scoped>
.container {
  padding: 20px;
}

.header {
   display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
</style>
