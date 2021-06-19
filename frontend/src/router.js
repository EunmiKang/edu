
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import EduApplicationManager from "./components/EduApplicationManager"

import EducationManager from "./components/EducationManager"


import Mypage from "./components/Mypage"
import PaymentManager from "./components/PaymentManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/EduApplication',
                name: 'EduApplicationManager',
                component: EduApplicationManager
            },

            {
                path: '/Education',
                name: 'EducationManager',
                component: EducationManager
            },


            {
                path: '/Mypage',
                name: 'Mypage',
                component: Mypage
            },
            {
                path: '/Payment',
                name: 'PaymentManager',
                component: PaymentManager
            },



    ]
})
