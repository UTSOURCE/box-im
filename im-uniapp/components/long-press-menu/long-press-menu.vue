<template>
	<view class="long-press-menu none-pointer-events">
		<view @longpress.prevent.stop="onLongPress($event)" @touchmove="onTouchMove" @touchend="onTouchEnd">
			<slot></slot>
		</view>
		<view v-if="isShowMenu" class="menu-mask" @touchstart="onClose()" @contextmenu.prevent=""></view>
		<view v-if="isShowMenu" class="menu" :style="menuStyle">
			<view class="menu-item" v-for="(item) in items" :key="item.key" @click.prevent="onSelectMenu(item)">
				<text :style="itemStyle(item)"> {{ item.name }}</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: "long-press-menu",
	data() {
		return {
			isShowMenu: false,
			isTouchMove: false,
			style: ""
		}
	},
	props: {
		items: {
			type: Array
		}
	},
	methods: {
		onLongPress(e) {
			if (this.isTouchMove) {
				// 屏幕移动时不弹出
				return;
			}
			uni.getSystemInfo({
				success: (res) => {
					let touches = e.touches[0];
					let style = "";
					/* 因 非H5端不兼容 style 属性绑定 Object ，所以拼接字符 */
					if (touches.clientY > (res.windowHeight / 2)) {
						style = `bottom:${res.windowHeight - touches.clientY + 20}px;`;
					} else {
						style = `top:${touches.clientY + 20}px;`;
					}
					if (touches.clientX > (res.windowWidth / 2)) {
						style += `right:${res.windowWidth - touches.clientX}px;`;
					} else {
						style += `left:${touches.clientX}px;`;
					}
					this.menuStyle = style;
					//
					this.$nextTick(() => {
						this.isShowMenu = true;
					});
					this.menuTouch = touches
				}
			})
		},
		onTouchMove(e) {
			this.isTouchMove = true;
			let touches = e.touches[0];
			// 屏幕拖动大于50px时，取消菜单
			if (Math.abs(touches.clientX - this.menuTouch.clientX) > 50 ||
				Math.abs(touches.clientY - this.menuTouch.clientY) > 50) {
				this.onClose();
			}
		},
		onTouchEnd() {
			this.isTouchMove = false;
		},
		onSelectMenu(item) {
			this.$emit("select", item);
			this.isShowMenu = false;
		},
		onClose() {
			this.isShowMenu = false;
		},
		itemStyle(item) {
			if (item.color) {
				return `color:${item.color};`
			}
			return `color:#000;`;
		}
	}
}
</script>

<style lang="scss" scoped>
.long-press-menu {
	.menu-mask {
		position: fixed;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		width: 100%;
		height: 100%;
		z-index: 999;
	}

	.menu {
		position: fixed;
		border-radius: 4px;
		overflow: hidden;
		background-color: #fff;
		z-index: 1000;
		box-shadow: $im-box-shadow-dark;

		.menu-item {
			height: 28px;
			min-width: 120rpx;
			line-height: 28px;
			font-size: $im-font-size-small;
			display: flex;
			padding: 6px 20px;
			justify-content: flex-start;

			&:hover {
				background: $im-bg-active;
			}

			.menu-icon {
				margin-right: 10rpx;
			}
		}
	}
}
</style>