
class BaseDB {

  constructor() {
    this.userId = null;
  }

  async open(userId) {
    this.userId = userId
  }

  async close() {

  }

  buildConversationKey(type, targetId) {
    return type + '-' + targetId;
  }
}

export default BaseDB;