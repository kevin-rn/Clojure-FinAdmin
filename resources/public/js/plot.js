// Color range information for interpolation
const colorRangeInfo = { colorStart: 0, colorEnd: 1, useEndAsStart: false };

/**
 * Calculates a point on the color scale based on the index and interval size.
 * 
 * @param {number} i - The index in the data range.
 * @param {number} intervalSize - The size of each interval on the color scale.
 * @returns {number} - The calculated color point.
 */
function calculatePoint(i, intervalSize) {
    const { colorStart, colorEnd, useEndAsStart } = colorRangeInfo;
    return (useEndAsStart
        ? (colorEnd - (i * intervalSize))
        : (colorStart + (i * intervalSize)));
}

/**
 * Interpolates a range of colors for a given data length using a color scale.
 * 
 * @param {number} dataLength - The number of data points to generate colors for.
 * @param {function} colorScale - The color scale function to use for color interpolation.
 * @returns {Array} - An array of interpolated color values.
 */
function interpolateColors(dataLength, colorScale) {
    const { colorStart, colorEnd } = colorRangeInfo;
    const colorRange = colorEnd - colorStart;
    const intervalSize = colorRange / dataLength;
    let colorArray = [];

    for (let i = 0; i < dataLength; i++) {
        const colorPoint = calculatePoint(i, intervalSize);
        colorArray.push(colorScale(colorPoint));
    }
    return colorArray;
}

/**
 * Creates a pie chart representing expense types using Chart.js.
 * The colors of the chart are interpolated using the D3 color scale.
 */
function createExpensePieChart() {
    const colorScale = d3.interpolateInferno;
    const ctx = document.getElementById('expensePieChart').getContext("2d");
    const dataLength = Object.keys(expenseTypeData).length;
    
    // Interpolate colors based on the number of expense types
    const COLORS = interpolateColors(dataLength, colorScale);

    new Chart(ctx, {
        type: "pie",
        data: {
            labels: Object.keys(expenseTypeData),
            datasets: [{
                label: "Expense Types Count",
                data: Object.values(expenseTypeData),
                backgroundColor: COLORS,
                borderColor: COLORS,
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: "right"
                },
                maintainAspectRatio: false,
                tooltip: {
                    callbacks: {
                        label: function (tooltipItem) {
                            return tooltipItem.label + ": " + tooltipItem.raw + " occurrences";
                        }
                    }
                }
            }
        }
    });
}

/**
 * Creates a stacked bar chart displaying monthly data using Chart.js.
 * The colors of each dataset are interpolated using the D3 color scale.
 */
function createStackedBarChart() {
    const colorScale = d3.interpolateInferno;
    const ctx = document.getElementById("stackedBarChart").getContext("2d");
    
    // Interpolate colors based on the number of datasets in chartData
    const COLORS = interpolateColors(chartData.length, colorScale);

    // Define the months for the X-axis labels
    const labels = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

    // Assign interpolated colors to each dataset
    chartData = chartData.map((dataset, index) => ({
        ...dataset,
        backgroundColor: COLORS[index]
    }));

    const data = {
        labels: labels,
        datasets: chartData
    };

    new Chart(ctx, {
        type: 'bar',
        data: data,
        options: {
            plugins: {
                title: {
                    display: false,
                },
                legend: {
                    display: false,
                },
            },
            responsive: true,
            interaction: {
                intersect: false,
            },
            scales: {
                x: {
                    stacked: true,
                },
                y: {
                    stacked: true,
                }
            }
        }
    });
}
