
function createTransactionPlot() {
  const ctx = document.getElementById('transactionChart').getContext("2d");
  new Chart(ctx, {
    type: "bar",
    data: {
        labels: transactionData.map(t => t.type),
        datasets: [{
            label: "Total Amount",
            data: transactionData.map(t => t.amount),
            backgroundColor: ["blue", "red", "green"],
            borderColor: ["darkblue", "darkred", "darkgreen"],
            borderWidth: 1
        }]
    },
    options: {
        responsive: true,
        indexAxis: "y",
        plugins: {
          legend: {
              display: false
          }
      },
        scales: {
            x: {
                beginAtZero: true
            }
        }
      }
  });
}