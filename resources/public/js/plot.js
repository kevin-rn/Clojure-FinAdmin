// https://medium.com/code-nebula/automatically-generate-chart-colors-with-chart-js-d3s-color-scales-f62e282b2b41 
// https://github.com/code-nebula/chart-color-generator/tree/master
const colorRangeInfo = {colorStart: 0, colorEnd: 1, useEndAsStart: false};

function calculatePoint(i, intervalSize) {
    var { colorStart, colorEnd, useEndAsStart } = colorRangeInfo;
    return (useEndAsStart
        ? (colorEnd - (i * intervalSize))
        : (colorStart + (i * intervalSize)));
}

function interpolateColors(dataLength, colorScale) {
    var { colorStart, colorEnd } = colorRangeInfo;
    var colorRange = colorEnd - colorStart;
    var intervalSize = colorRange / dataLength;
    var i, colorPoint;
    var colorArray = [];
    for (i = 0; i < dataLength; i++) {
        colorPoint = calculatePoint(i, intervalSize, colorRangeInfo);
        colorArray.push(colorScale(colorPoint));
    }
    return colorArray;
}

function createExpensePieChart() {
    const colorScale = d3.interpolateInferno;
    const ctx = document.getElementById('expensePieChart').getContext("2d");
    const dataLength = Object.keys(expenseTypeData).length;
    var COLORS = interpolateColors(dataLength, colorScale, colorRangeInfo);
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
          maintainAspectRatio : false,
          tooltip: {
            callbacks: {
              label: function(tooltipItem) {
                return tooltipItem.label + ": " + tooltipItem.raw + " occurrences";
              }
            }
          }
        }
      }
    });
}

function createStackedBarChart() {
    const colorScale = d3.interpolateInferno;
    var COLORS = interpolateColors(chartData.length, colorScale, colorRangeInfo);
    const ctx = document.getElementById("stackedBarChart").getContext("2d");
    const labels = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

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
