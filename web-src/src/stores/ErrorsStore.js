import { observable } from 'mobx';

export default class ErrorsStore {
    @observable errors = [];

    addError(text) {
        this.errors.push(text);
    }

    removeError(error) {
        this.errors.remove(error);
    }
};