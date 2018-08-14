import React from 'react';
import { shallow, mount } from 'enzyme';
import CurrentTask from '../src/CurrentTask.jsx';
import TaskTypesStore from '../src/stores/TaskTypesStore.js';
import ErrorsStore from '../src/stores/ErrorsStore.js';

describe('CurrentTask', () => {
    let taskStore;
    let errorsStore;

    beforeEach(() => {
        taskStore = new TaskTypesStore();
        errorsStore = new ErrorsStore();

        fetch.resetMocks();
    });

    const createComponent = function() {
        return <CurrentTask taskStore={ taskStore } errorsStore={ errorsStore } />;
    }

    it('renders', () => {
        const component = shallow(createComponent());

        expect(component).toBeDefined();
    });

    it('fetches, shows, and selects the current task on mount', () => {
        const task = { category: 'Test', name: 'Test' };
        fetch.mockResponse(JSON.stringify(task));
        const component = mount(createComponent());

        setImmediate(() => {
            component.update();

            const componentRoot = component.find('.current-task');
            const currentTaskLabel = componentRoot.children('div').at(0);

            expect(currentTaskLabel.text()).toBe(`Current task: ${task.name}`);

            const dropdown = componentRoot.find('select');
            expect(dropdown.instance().value).toBe(JSON.stringify(task));
        });
    });
    
    it('shows the default task if user has no current task', () => {
        fetch.mockResponse('', { status: 204 });
        const component = mount(createComponent());

        setImmediate(() => {
            component.update();

            const componentRoot = component.find('.current-task');
            const currentTaskLabel = componentRoot.children('div').at(0);

            expect(currentTaskLabel.text()).toBe(`Current task: None`);

            const dropdown = componentRoot.find('select');
            const selectedTask = JSON.parse(dropdown.instance().value);
            expect(selectedTask.category).toBe('None');
            expect(selectedTask.name).toBe('None');
        });
    });

    it('populates the dropdown with all the available task types plus the default', () => {
        const taskTypes = [
            { category: 'Test', name: 'a' },
            { category: 'Test', name: 'Test' },
            { category: 'Other', name: 'blah' }
        ];
        taskStore.taskTypes.push(...taskTypes);
        const task = { category: 'Test', name: 'Test' };
        fetch.mockResponse(JSON.stringify(task));
        const component = mount(createComponent());

        setImmediate(() => {
            component.update();

            const dropdownOptions = component.find('option');
            expect(dropdownOptions.at(0).text()).toBe('Test');
            expect(dropdownOptions.at(1).text()).toBe('  a');
            expect(dropdownOptions.at(2).text()).toBe('  Test');
            expect(dropdownOptions.at(3).text()).toBe('Other');
            expect(dropdownOptions.at(4).text()).toBe('  blah');
            expect(dropdownOptions.at(5).text()).toBe('None');
            expect(dropdownOptions.at(6).text()).toBe('  None');            
        });
    });

    it('does not allow the user to select a category', () => {
        const task = { category: 'None', name: 'None' };
        fetch.mockResponse(JSON.stringify(task));
        const component = mount(createComponent());

        setImmediate(() => {
            component.update();

            const testHeading = component.find('option').findWhere((option) => option.text() === 'None');
            expect(testHeading.instance().disabled).toBeTruthy();
        });
    });

    it('still shows the dropdown even if loading current task fails', () => {
        const taskTypes = [
            { category: 'Test', name: 'a' },
            { category: 'Test', name: 'Test' },
            { category: 'Other', name: 'blah' }
        ];
        taskStore.taskTypes.push(...taskTypes);
        const task = { category: 'Test', name: 'Test' };
        fetch.mockReject(new Error('Request timeout'));
        const component = mount(createComponent());

        setImmediate(() => {
            component.update();

            expect(component.html()).toBeNull();
        });
    })

    describe('onChange', () => {
        it('submits the new value to the server and updates on success', (done) => {
            const taskTypes = [
                { category: 'Test', name: 'Test' }
            ];
            taskStore.taskTypes.push(...taskTypes);
            const task = { category: 'None', name: 'None' };
            fetch.mockResponseOnce(JSON.stringify(task));
            const component = mount(createComponent());
    
            setImmediate(() => {
                component.update();

                fetch.mockResponseOnce(JSON.stringify(true));
                const newTask = JSON.stringify(taskTypes[0]);
                component.find('select').simulate('change', { target: { value: newTask } });
                
                setImmediate(() => {
                    component.update();

                    expect(fetch.mock.calls.length).toBe(2);
                    const dropdown = component.find('select');
                    expect(dropdown.instance().value).toBe(newTask);

                    done();
                });
            });
        });

        it('submits the new value to the server and queues an error message on rejection', (done) => {
            const taskTypes = [
                { category: 'Test', name: 'Test' }
            ];
            taskStore.taskTypes.push(...taskTypes);
            const task = { category: 'None', name: 'None' };
            fetch.mockResponseOnce(JSON.stringify(task));
            const component = mount(createComponent());
    
            setImmediate(() => {
                component.update();

                fetch.mockResponseOnce(JSON.stringify(false));
                const newTask = JSON.stringify(taskTypes[0]);
                component.find('select').simulate('change', { target: { value: newTask } });
                
                setImmediate(() => {
                    component.update();

                    expect(fetch.mock.calls.length).toBe(2);
                    const dropdown = component.find('select');
                    expect(dropdown.instance().value).toBe(JSON.stringify(task));

                    expect(errorsStore.errors.length).toBe(1);

                    done();
                });
            });
        });
        
        it('submits the new value to the server and queues an error message on request failure', (done) => {
            const taskTypes = [
                { category: 'Test', name: 'Test' }
            ];
            taskStore.taskTypes.push(...taskTypes);
            const task = { category: 'None', name: 'None' };
            fetch.mockResponseOnce(JSON.stringify(task));
            const component = mount(createComponent());
    
            setImmediate(() => {
                component.update();

                fetch.mockRejectOnce(new Error('!!!!'));
                const newTask = taskTypes[0];
                component.find('select').simulate('change', { target: { value: JSON.stringify(newTask) } });
                
                setImmediate(() => {
                    component.update();

                    expect(fetch.mock.calls.length).toBe(2);
                    const dropdown = component.find('select');
                    expect(dropdown.instance().value).toBe(JSON.stringify(task));

                    expect(errorsStore.errors.length).toBe(1);

                    done();
                });
            });
        });
    });
});