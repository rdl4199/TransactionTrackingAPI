const currencyFormatter = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
});

const transactionForm = document.getElementById("transactionForm");
const importForm = document.getElementById("importForm");
const refreshButton = document.getElementById("refreshButton");
const statusMessage = document.getElementById("statusMessage");
const totalAmount = document.getElementById("totalAmount");
const transactionCount = document.getElementById("transactionCount");
const categoryCount = document.getElementById("categoryCount");
const transactionTableBody = document.getElementById("transactionTableBody");

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("date").value = new Date().toISOString().split("T")[0];
    refreshDashboard();
});

transactionForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const submitButton = transactionForm.querySelector("button[type='submit']");
    submitButton.disabled = true;

    const payload = {
        date: document.getElementById("date").value,
        description: document.getElementById("description").value.trim(),
        amount: Number.parseFloat(document.getElementById("amount").value),
        category: document.getElementById("category").value.trim()
    };

    try {
        const response = await fetch("/transactions", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error("Transaction could not be stored.");
        }

        transactionForm.reset();
        document.getElementById("date").value = new Date().toISOString().split("T")[0];
        setStatus("Transaction stored successfully.");
        await refreshDashboard();
    } catch (error) {
        setStatus(error.message, true);
    } finally {
        submitButton.disabled = false;
    }
});

importForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const submitButton = importForm.querySelector("button[type='submit']");
    submitButton.disabled = true;

    const fileInput = document.getElementById("csvFile");
    if (!fileInput.files.length) {
        setStatus("Choose a CSV file before importing.", true);
        submitButton.disabled = false;
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);

    try {
        const response = await fetch("/transactions/import", {
            method: "POST",
            body: formData
        });

        const result = await response.json();
        if (!response.ok) {
            throw new Error(result.error || result.message || "Import failed.");
        }

        importForm.reset();
        setStatus(`${result.message}. Rows imported: ${result.rowsImported}.`);
        await refreshDashboard();
    } catch (error) {
        setStatus(error.message, true);
    } finally {
        submitButton.disabled = false;
    }
});

refreshButton.addEventListener("click", () => {
    refreshDashboard();
});

async function refreshDashboard() {
    try {
        const [transactionsResponse, summaryResponse] = await Promise.all([
            fetch("/transactions"),
            fetch("/transactions/summary/total")
        ]);

        if (!transactionsResponse.ok || !summaryResponse.ok) {
            throw new Error("Could not load transaction data.");
        }

        const transactions = await transactionsResponse.json();
        const summary = await summaryResponse.json();

        renderTransactions(transactions);
        renderStats(transactions, summary.total);
        setStatus(`Loaded ${transactions.length} transaction${transactions.length === 1 ? "" : "s"}.`);
    } catch (error) {
        setStatus(error.message, true);
    }
}

function renderStats(transactions, total) {
    totalAmount.textContent = currencyFormatter.format(Number(total || 0));
    transactionCount.textContent = transactions.length.toString();

    const categories = new Set(
        transactions
            .map((transaction) => transaction.category)
            .filter((category) => category && category.trim().length > 0)
    );
    categoryCount.textContent = categories.size.toString();
}

function renderTransactions(transactions) {
    const sortedTransactions = [...transactions].sort((left, right) => {
        const leftDate = left.date || "";
        const rightDate = right.date || "";
        return rightDate.localeCompare(leftDate);
    });

    if (!sortedTransactions.length) {
        transactionTableBody.innerHTML = `
            <tr class="empty-row">
                <td colspan="5">No transactions yet.</td>
            </tr>
        `;
        return;
    }

    transactionTableBody.innerHTML = sortedTransactions.map((transaction) => {
        const amount = Number(transaction.amount || 0);
        const amountClass = amount < 0 ? "amount-negative" : "amount-positive";

        return `
            <tr>
                <td>${escapeHtml(transaction.date || "-")}</td>
                <td>${escapeHtml(transaction.description || "-")}</td>
                <td>${escapeHtml(transaction.category || "-")}</td>
                <td class="${amountClass}">${currencyFormatter.format(amount)}</td>
                <td>
                    <button class="table-action" type="button" data-id="${transaction.id}">Delete</button>
                </td>
            </tr>
        `;
    }).join("");

    for (const button of transactionTableBody.querySelectorAll("[data-id]")) {
        button.addEventListener("click", () => deleteTransaction(button.dataset.id));
    }
}

async function deleteTransaction(id) {
    try {
        const response = await fetch(`/transactions/${id}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            throw new Error("Transaction could not be deleted.");
        }

        setStatus(`Transaction ${id} deleted.`);
        await refreshDashboard();
    } catch (error) {
        setStatus(error.message, true);
    }
}

function setStatus(message, isError = false) {
    statusMessage.textContent = message;
    statusMessage.style.color = isError ? "#ffb4b4" : "#c6f8eb";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}
