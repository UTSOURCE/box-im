<template>
	<view class="chat-message-item">
		<view class="message-tip" v-if="msgInfo.type == $enums.MESSAGE_TYPE.TIP_TEXT">
			{{ msgInfo.content }}
		</view>
		<view class="message-tip" v-else-if="msgInfo.type == $enums.MESSAGE_TYPE.TIP_TIME">
			{{ $date.toTimeText(msgInfo.sendTime) }}
		</view>
		<view class="message-normal" v-else-if="isNormal" :class="{ 'message-mine': msgInfo.selfSend }">
			<head-image class="avatar" @longpress.prevent="$emit('longPressHead')" :id="msgInfo.sendId" :url="headImage"
				:name="showName" size="small"></head-image>
			<view class="content">
				<view v-if="msgInfo.groupId && !msgInfo.selfSend" class="top">
					<text>{{ showName }}</text>
				</view>
				<view class="bottom">
					<view class="message-content-wrapper">
						<view v-if="msgInfo.type == $enums.MESSAGE_TYPE.TEXT">
							<long-press-menu :items="menuItems" @select="onSelectMenu">
								<!-- up-parse支持点击a标签,但是不支持显示emo表情，也不支持换行 -->
								<up-parse v-if="$url.containUrl(msgInfo.content)&&!$emo.containEmoji(msgInfo.content)"
									class="message-text" :showImgMenu="false" :content="nodesText"></up-parse>
								<!-- rich-text支持显示emo表情以及消息换行，但是不支持点击a标签 -->
								<rich-text v-else class="message-text" :nodes="nodesText"></rich-text>
							</long-press-menu>
						</view>
						<view class="message-image" v-else-if="msgInfo.type == $enums.MESSAGE_TYPE.IMAGE">
							<long-press-menu :items="menuItems" @select="onSelectMenu">
								<view class="image-box">
									<image class="send-image" :style="imageStyle" mode="aspectFill"
										:src="contentData.thumbUrl" lazy-load="true" @click.stop="onShowFullImage()">
									</image>
									<loading v-if="sending"></loading>
								</view>
							</long-press-menu>
						</view>
						<view class="message-file" v-else-if="msgInfo.type == $enums.MESSAGE_TYPE.FILE">
							<long-press-menu :items="menuItems" @select="onSelectMenu">
								<view class="file-box">
									<view class="file-info">
										<uni-link class="file-name" :text="contentData.name" showUnderLine="true"
											color="#007BFF" :href="contentData.url"></uni-link>
										<view class="file-size">{{ fileSize }}</view>
									</view>
									<view class="file-icon iconfont icon-file"></view>
									<loading v-if="sending"></loading>
								</view>
							</long-press-menu>
						</view>
						<long-press-menu v-else-if="msgInfo.type == $enums.MESSAGE_TYPE.AUDIO" :items="menuItems"
							@select="onSelectMenu">
							<view class="message-audio message-text" @click="onPlayAudio()">
								<text class="iconfont icon-voice-play"></text>
								<text class="chat-audio-text">{{ contentData.duration + '"' }}</text>
								<text v-if="audioPlayState == 'PAUSE'" class="iconfont icon-play"></text>
								<text v-if="audioPlayState == 'PLAYING'" class="iconfont icon-pause"></text>
							</view>
						</long-press-menu>
						<view v-if="sending&&isTextMessage" class="sending">
							<loading :size="40" icon-color="#656adf" :mask="false"></loading>
						</view>
						<view v-else-if="sendFail" @click="onSendFail"
							class="send-fail iconfont icon-warning-circle-fill"></view>
					</view>
					<long-press-menu v-if="isAction" :items="menuItems" @select="onSelectMenu">
						<view class="chat-realtime message-text" @click="$emit('call')">
							<text v-if="msgInfo.type == $enums.MESSAGE_TYPE.ACT_RT_VOICE"
								class="iconfont icon-chat-voice"></text>
							<text v-if="msgInfo.type == $enums.MESSAGE_TYPE.ACT_RT_VIDEO"
								class="iconfont icon-chat-video"></text>
							<text>{{ msgInfo.content }}</text>
						</view>
					</long-press-menu>
					<view class="message-status" v-if="!isAction && msgInfo.selfSend && !msgInfo.groupId">
						<text class="chat-readed" v-if="msgInfo.status == $enums.MESSAGE_STATUS.READED">已读</text>
						<text class="chat-unread" v-else>未读</text>
					</view>
					<view class="chat-receipt" v-if="msgInfo.receipt" @click="onShowReadedBox">
						<text v-if="msgInfo.receiptOk" class="tool-icon iconfont icon-ok"></text>
						<text v-else>{{ msgInfo.readedCount }}人已读</text>
					</view>
				</view>
			</view>
		</view>
		<chat-group-readed ref="chatGroupReaded" :groupMembers="groupMembers" :msgInfo="msgInfo"></chat-group-readed>
	</view>
</template>

<script>
export default {
	name: "chat-message-item",
	props: {
		headImage: {
			type: String,
			required: true
		},
		showName: {
			type: String,
			required: true
		},
		msgInfo: {
			type: Object,
			required: true
		},
		groupMembers: {
			type: Array
		}
	},
	data() {
		return {
			audioPlayState: 'STOP',
			innerAudioContext: null,
			menu: {
				show: false,
				style: ""
			}
		}
	},
	methods: {
		onSendFail() {
			this.$emit("resend", this.msgInfo);
		},
		onPlayAudio() {
			// 初始化音频播放器
			if (!this.innerAudioContext) {
				this.innerAudioContext = uni.createInnerAudioContext();
				let url = this.contentData.url;
				this.innerAudioContext.src = url;
				this.innerAudioContext.onEnded((e) => {
					console.log('停止')
					this.audioPlayState = "STOP"
					this.emit();
				})
				this.innerAudioContext.onError((e) => {
					this.audioPlayState = "STOP"
					console.log("播放音频出错");
					console.log(e)
					this.emit();
				});
			}
			if (this.audioPlayState == 'STOP') {
				this.innerAudioContext.play();
				this.audioPlayState = "PLAYING";
			} else if (this.audioPlayState == 'PLAYING') {
				this.innerAudioContext.pause();
				this.audioPlayState = "PAUSE"
			} else if (this.audioPlayState == 'PAUSE') {
				this.innerAudioContext.play();
				this.audioPlayState = "PLAYING"
			}
			this.emit();
		},
		onSelectMenu(item) {
			this.$emit(item.key.toLowerCase(), this.msgInfo);
			this.menu.show = false;
		},
		onShowFullImage() {
			let imageUrl = this.contentData.originUrl;
			uni.previewImage({
				urls: [imageUrl]
			})
		},
		onShowReadedBox() {
			this.$refs.chatGroupReaded.open();
		},
		emit() {
			this.$emit("audioStateChange", this.audioPlayState, this.msgInfo);
		},
		stopPlayAudio() {
			if (this.innerAudioContext) {
				this.innerAudioContext.stop();
				this.innerAudioContext = null;
				this.audioPlayState = "STOP"
			}
		}
	},
	computed: {
		sending() {
			return this.msgInfo.status == this.$enums.MESSAGE_STATUS.SENDING;
		},
		sendFail() {
			return this.msgInfo.status == this.$enums.MESSAGE_STATUS.FAILED;
		},
		contentData() {
			return JSON.parse(this.msgInfo.content)
		},
		fileSize() {
			let size = this.contentData.size;
			if (size > 1024 * 1024) {
				return Math.round(size / 1024 / 1024) + "M";
			}
			if (size > 1024) {
				return Math.round(size / 1024) + "KB";
			}
			return size + "B";
		},
		menuItems() {
			let items = [];
			if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.TEXT) {
				items.push({
					key: 'COPY',
					name: '复制',
					icon: 'bars'
				});
			}
			if (this.msgInfo.selfSend && this.msgInfo.id > 0) {
				items.push({
					key: 'RECALL',
					name: '撤回',
					icon: 'refreshempty'
				});
			}
			items.push({
				key: 'DELETE',
				name: '删除',
				icon: 'trash',
				color: '#e64e4e'
			});
			if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.FILE) {
				items.push({
					key: 'DOWNLOAD',
					name: '下载并打开',
					icon: 'download'
				});
			}
			return items;
		},
		isTextMessage() {
			return this.msgInfo.type == this.$enums.MESSAGE_TYPE.TEXT
		},
		isAction() {
			return this.$msgType.isAction(this.msgInfo.type);
		},
		isNormal() {
			const type = this.msgInfo.type;
			return this.$msgType.isNormal(type) || this.$msgType.isAction(type)
		},
		nodesText() {
			let color = this.msgInfo.selfSend ? 'white' : '';
			let text = this.$str.html2Escape(this.msgInfo.content)
			text = this.$url.replaceURLWithHTMLLinks(text, color)
			return this.$emo.transform(text, 'emoji-normal')
		},
		imageStyle() {
			console.log(uni.getSystemInfo())
			let maxSize = uni.getSystemInfoSync().windowWidth * 0.6;
			let minSize = uni.getSystemInfoSync().windowWidth * 0.2;
			let width = this.contentData.width;
			let height = this.contentData.height;
			if (width && height) {
				let ratio = Math.min(width, height) / Math.max(width, height);
				let w = Math.max(width > height ? maxSize : ratio * maxSize, minSize);
				let h = Math.max(width > height ? ratio * maxSize : maxSize, minSize);
				return `width: ${w}px;height:${h}px;`
			} else {
				// 兼容历史版本，历史数据没有记录宽高
				return `max-width: ${maxSize}px;min-width:100px;max-height: ${maxSize}px;min-height:100px;`
			}
		}
	}
}
</script>

<style scoped lang="scss">
.chat-message-item {
	padding: 2rpx 20rpx;

	.message-tip {
		line-height: 60rpx;
		text-align: center;
		color: $im-text-color-lighter;
		font-size: $im-font-size-smaller-extra;
		padding: 10rpx;
	}

	.message-normal {
		position: relative;
		margin-bottom: 22rpx;
		padding-left: 110rpx;
		min-height: 80rpx;

		.avatar {
			position: absolute;
			top: 0;
			left: 0;
		}

		.content {
			text-align: left;

			.top {
				display: flex;
				flex-wrap: nowrap;
				color: $im-text-color-lighter;
				font-size: $im-font-size-smaller;
				line-height: $im-font-size-smaller;
				height: $im-font-size-smaller;
			}

			.bottom {
				display: inline-block;
				padding-right: 80rpx;
				margin-top: 5rpx;

				.message-content-wrapper {
					position: relative;
					display: flex;
					align-items: center;

					.sending {
						position: relative;
						margin: 0 6rpx;

						.icon-loading {
							color: $im-color-primary;
						}
					}

					.send-fail {
						color: #e60c0c;
						font-size: 50rpx;
						margin: 0 5rpx;
					}

					.message-text {
						position: relative;
						line-height: 1.6;
						margin-top: 10rpx;
						padding: 16rpx 24rpx;
						background-color: $im-bg;
						border-radius: 20rpx;
						color: $im-text-color;
						font-size: $im-font-size;
						text-align: left;
						display: block;
						word-break: break-word;
						white-space: pre-line;

						&:after {
							content: "";
							position: absolute;
							left: -20rpx;
							top: 26rpx;
							width: 6rpx;
							height: 6rpx;
							border-style: solid dashed dashed;
							border-color: $im-bg transparent transparent;
							overflow: hidden;
							border-width: 18rpx;
						}
					}

					.message-image {
						display: flex;
						flex-wrap: nowrap;
						flex-direction: row;
						align-items: center;

						.image-box {
							position: relative;

							.send-image {
								cursor: pointer;
								border-radius: 10rpx;
								background: $im-bg;
								border: 6rpx solid $im-color-primary-light-5;
							}
						}

						.send-fail {
							color: $im-color-danger;
							font-size: $im-font-size;
							cursor: pointer;
							margin: 0 20px;
						}
					}

					.message-file {
						display: flex;
						flex-wrap: nowrap;
						flex-direction: row;
						align-items: center;
						cursor: pointer;

						.file-box {
							position: relative;
							display: flex;
							flex-wrap: nowrap;
							align-items: center;
							min-height: 60px;
							border-radius: 4px;
							padding: 10px 15px;
							box-shadow: $im-box-shadow-dark;

							.file-info {
								flex: 1;
								height: 100%;
								text-align: left;
								font-size: 14px;
								width: 300rpx;

								.file-name {
									font-weight: 600;
									margin-bottom: 15px;
									word-break: break-all;
								}
							}

							.file-icon {
								font-size: 80rpx;
								color: #d42e07;
							}
						}

						.send-fail {
							color: #e60c0c;
							font-size: 50rpx;
							cursor: pointer;
							margin: 0 20rpx;
						}
					}

					.message-audio {
						display: flex;
						align-items: center;

						.chat-audio-text {
							padding-right: 8px;
						}

						.icon-voice-play {
							font-size: 18px;
							padding-right: 8px;
						}
					}
				}

				.chat-realtime {
					display: flex;
					align-items: center;

					.iconfont {
						font-size: 20px;
						padding-right: 8px;
					}
				}

				.message-status {
					line-height: $im-font-size-smaller-extra;
					font-size: $im-font-size-smaller-extra;
					padding-top: 2rpx;

					.chat-readed {
						display: block;
						padding-top: 2rpx;
						color: $im-text-color-lighter;
					}

					.chat-unread {
						color: $im-color-danger;
					}
				}

				.chat-receipt {
					font-size: $im-font-size-smaller;
					color: $im-text-color-lighter;
					font-weight: 600;

					.icon-ok {
						font-size: 20px;
						color: $im-color-success;
					}
				}
			}
		}

		&.message-mine {
			text-align: right;
			padding-left: 0;
			padding-right: 110rpx;

			.avatar {
				left: auto;
				right: 0;
			}

			.content {
				text-align: right;

				.bottom {
					padding-left: 80rpx;
					padding-right: 0;

					.message-content-wrapper {
						flex-direction: row-reverse;
					}

					.message-text {
						margin-left: 10px;
						background-color: $im-color-primary-light-2;
						color: #fff;

						&:after {
							left: auto;
							right: -9px;
							border-top-color: $im-color-primary-light-2;
						}
					}

					.message-audio {
						flex-direction: row-reverse;

						.chat-audio-text {
							padding-right: 0;
							padding-left: 8px;
						}

						.icon-voice-play {
							transform: rotateY(180deg);
						}
					}

					.chat-realtime {
						display: flex;
						flex-direction: row-reverse;

						.iconfont {
							transform: rotateY(180deg);
						}
					}
				}
			}
		}
	}
}
</style>