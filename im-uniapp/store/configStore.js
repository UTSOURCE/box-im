import { defineStore } from 'pinia';
import http from '../common/request'

export default defineStore('configStore', {
	state: () => {
		return {
			appInit: false,
			webrtc: {}
		}
	},
	actions: {
		setConfig(config) {
			this.webrtc = config.webrtc;
		},
		setAppInit(appInit) {
			this.appInit = appInit;
		},
		clear() {
			this.webrtc = {};
		},
		loadConfig() {
			return new Promise((resolve, reject) => {
				http({
					url: '/system/config',
					method: 'GET'
				}).then((config) => {
					console.log("系统配置", config)
					this.setConfig(config);
					resolve();
				}).catch((res) => {
					reject(res);
				});
			})
		}
	}
})