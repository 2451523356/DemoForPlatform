<template>
  <div class="login">
    <el-card class="login-card">
      <h2>用户登录</h2>
      <el-form ref="loginForm" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">登录</el-button>
        </el-form-item>
        <el-form-item>
          <span>还没有账号？</span>
          <el-button type="text" @click="$router.push('/register')">立即注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'Login',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    ...mapActions('user', ['login']),
    handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          this.loading = true
          try {
            console.log('登录请求数据:', this.form)
            const res = await this.$http.post('/auth/login', this.form)
            console.log('登录响应:', res)

            // 检查响应数据结构
            if (!res || !res.token || !res.user) {
              throw new Error('登录响应数据格式错误')
            }

            this.login({
              token: res.token,
              userInfo: res.user
            })
            this.$message.success('登录成功')
            // 如果是管理员，跳转到后台管理页面
            console.log('User role:', res.user.role)
            if (res.user.role === 'ADMIN') {
              console.log('Redirecting to admin dashboard')
              this.$router.push('/admin/dashboard')
            } else {
              this.$router.push('/')
            }
          } catch (error) {
            console.log('登录错误:', error)
            this.$message.error(error.response?.data?.message || error.message || '登录失败')
          } finally {
            this.loading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px);

  .login-card {
    width: 400px;
    padding: 20px;

    h2 {
      text-align: center;
      margin-bottom: 30px;
      color: #303133;
    }
  }
}
</style>
