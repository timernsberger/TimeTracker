import colorGenerator from '../../src/history/colorGenerator.js';

describe('colorGenerator', () => {
    let generator;
    beforeEach(() => {
        generator = colorGenerator();
    });

    it('generates colors', () => {
        const color = generator.next().value;
        expect(color.backgroundColor).toBeDefined();
        expect(color.borderColor).toBeDefined();
    });
    it('generates unlimited colors', () => {
        for(let i = 0; i < 100; i++) {
            generator.next();
        }
        const color = generator.next().value;
        expect(color).toBeDefined();
    });
});