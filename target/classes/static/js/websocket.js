// STOMP WebSocket client — connects and dispatches price updates

let stompClient = null;
let priceUpdateCallbacks = [];

function onPriceUpdate(callback) {
  priceUpdateCallbacks.push(callback);
}

function connectWebSocket() {
  const socket = new SockJS('/ws');
  stompClient = new StompJs.Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      console.log('WebSocket connected');
      stompClient.subscribe('/topic/prices', (msg) => {
        const stocks = JSON.parse(msg.body);
        priceUpdateCallbacks.forEach(cb => cb(stocks));
      });
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame);
    }
  });
  stompClient.activate();
}

function disconnectWebSocket() {
  if (stompClient) stompClient.deactivate();
}
