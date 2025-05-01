const apiKeyInput = document.getElementById('apiKey');
const saveButton = document.getElementById('save');
const statusDiv = document.getElementById('status');

// Load saved API key when options page opens
function loadOptions() {
    chrome.storage.local.get(['githubApiKey'], (result) => {
        if (result.githubApiKey) {
            apiKeyInput.value = result.githubApiKey;
        }
    });
}

// Save API key to chrome.storage.local
function saveOptions() {
    const apiKey = apiKeyInput.value.trim();
    if (!apiKey) {
        statusDiv.textContent = 'Error: API Key cannot be empty.';
        statusDiv.style.color = 'red';
        return;
    }
    chrome.storage.local.set({ githubApiKey: apiKey }, () => {
        statusDiv.textContent = 'Options saved.';
        statusDiv.style.color = 'green';
        setTimeout(() => {
            statusDiv.textContent = '';
        }, 2000);
    });
}

document.addEventListener('DOMContentLoaded', loadOptions);
saveButton.addEventListener('click', saveOptions);