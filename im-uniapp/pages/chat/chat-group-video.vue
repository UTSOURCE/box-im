<template>
	<web-view class="page chat-group-video" id="chat-video-wv" :src="url"></web-view>
</template>

<script>
import UNI_APP from '@/.env.js'
export default {
	data() {
		return {
			url: "",
			wv: '',
		}
	},
	methods: {
		initUrl() {
			this.url = "/hybrid/html/rtc-group/index.html?";
			this.url += "baseUrl=" + UNI_APP.BASE_URL;
		},
	},
	onBackPress() {
		this.sendMessageToWebView("NAV_BACK", {})
	},
	onLoad(options) {
		// 构建url
		this.initUrl();
	}
}
</script>

<style lang="scss" scoped>
.chat-group-video {
}
</style>