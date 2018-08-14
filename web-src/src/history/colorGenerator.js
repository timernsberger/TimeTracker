
export default function* colorGenerator() {
    const colors = [
        {
            backgroundColor: 'rgba(99, 255, 132, 0.6)',
            borderColor: 'rgba(44, 215, 102, 1)'
        },
        {
            backgroundColor: 'rgba(99, 132, 255, 0.6)',
            borderColor: 'rgba(99, 132, 255, 1)'
        },
        {
            backgroundColor: 'rgba(255, 99, 132, 0.6)',
            borderColor: 'rgba(255, 99, 132, 1)'
        },
        {
            backgroundColor: 'rgba(255, 255, 132, 0.6)',
            borderColor: 'rgba(200, 200, 96, 1)'
        },
        {
            backgroundColor: 'rgba(255, 165, 0, 0.6)',
            borderColor: 'rgba(255, 127, 80, 1)'
        },
        {
            backgroundColor: 'rgba(192, 192, 192, 0.6)',
            borderColor: 'rgba(169, 169, 169, 1)'
        }
    ];
    let i = 0;
    while(true) {
        yield colors[i];
        i = (i + 1) % colors.length;
    }
}