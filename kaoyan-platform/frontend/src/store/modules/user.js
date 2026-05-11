import Cookies from 'js-cookie'

function safeParseUserInfo() {
  try {
    const raw = localStorage.getItem('userInfo')
    if (!raw || raw === 'undefined' || raw === 'null') return null
    return JSON.parse(raw)
  } catch (e) {
    localStorage.removeItem('userInfo')
    return null
  }
}

const state = {
  token: Cookies.get('token') || '',
  userInfo: safeParseUserInfo()
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    Cookies.set('token', token, { expires: 7 })
  },
  SET_USER_INFO(state, userInfo) {
    console.log('SET_USER_INFO:', userInfo)
    state.userInfo = userInfo
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  },
  CLEAR_USER(state) {
    state.token = ''
    state.userInfo = null
    Cookies.remove('token')
    localStorage.removeItem('userInfo')
  }
}

const actions = {
  login({ commit }, { token, userInfo }) {
    console.log('Login action:', { token, userInfo })
    commit('SET_TOKEN', token)
    commit('SET_USER_INFO', userInfo)
  },
  logout({ commit }) {
    commit('CLEAR_USER')
  },
  updateUserInfo({ commit }, userInfo) {
    commit('SET_USER_INFO', userInfo)
  }
}

const getters = {
  token: state => state.token,
  userInfo: state => state.userInfo,
  isLoggedIn: state => !!state.token
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
