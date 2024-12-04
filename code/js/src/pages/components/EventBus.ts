import {MessageOutputModel} from "../../models/output/MessagesOutputModel";

type EventHandler = (data: MessageOutputModel) => void;

class EventBus {
    private events: { [key: string]: EventHandler[] } = {};

    subscribe(event: string, handler: EventHandler) {
        if (!this.events[event]) {
            this.events[event] = [];
        }
        this.events[event].push(handler);
    }

    unsubscribe(event: string, handler: EventHandler) {
        if (!this.events[event]) return;
        this.events[event] = this.events[event].filter((h) => h !== handler);
    }

    emit(event: string, data: MessageOutputModel) {
        if (!this.events[event]) return;
        this.events[event].forEach((handler) => handler(data));
    }
}

const eventBus = new EventBus();
export default eventBus;
