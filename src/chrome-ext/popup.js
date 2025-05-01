// --- START OF FILE popup.js ---

const ownerInput = document.getElementById('owner');
const repoInput = document.getElementById('repo');
const pathInput = document.getElementById('path');
const searchInput = document.getElementById('searchInput'); // New search input
const findLineButton = document.getElementById('findLineButton'); // New find button
const lineNumberInput = document.getElementById('lineNumber');
const contentInput = document.getElementById('content');
const labelNameInput = document.getElementById('labelName');
const modifyButton = document.getElementById('modifyButton');
const statusDiv = document.getElementById('status');

let extractedLabelHint = '';
let apiKey = null;
const STORAGE_KEY_LAST_INPUT = 'lastInputValues';

// --- Helper Functions ---

// Display status messages (now disables find button too when loading)
function showStatus(message, isError = false, isLoading = false) {
    statusDiv.textContent = message;
    statusDiv.className = isError ? 'error' : (isLoading ? '' : 'success');

    const disableButtons = isLoading;
    modifyButton.disabled = disableButtons;
    findLineButton.disabled = disableButtons; // Disable find button too

    // Re-enable specifically if not loading and not a success message with a link
    // (which keeps the modify button enabled to allow further actions)
    if (!isLoading) {
       if (!isError && statusDiv.className === 'success' && statusDiv.innerHTML.includes('</a>')) {
           // Modify button enabled by updateFileContent logic already
           findLineButton.disabled = false; // Ensure find button is enabled
       } else {
           // General success or error
           modifyButton.disabled = false;
           findLineButton.disabled = false;
       }
    }
     // Ensure modify button stays enabled if find operation fails/succeeds without link
    if (!isLoading && !statusDiv.innerHTML.includes('</a>') && !message.startsWith('Updating')) {
        modifyButton.disabled = false;
    }
}

// Base64 encode (UTF-8 safe)
function b64EncodeUnicode(str) {
    return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g,
        function toSolidBytes(match, p1) {
            return String.fromCharCode('0x' + p1);
    }));
}

// Base64 decode (UTF-8 safe)
function b64DecodeUnicode(str) {
    return decodeURIComponent(atob(str).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
}


// --- GitHub API Interaction ---
// getFileContent and updateFileContent remain the same
async function getFileContent(owner, repo, path, operationDesc = 'Fetching file content') { // Added operation description
    const url = `https://api.github.com/repos/${owner}/${repo}/contents/${path}`;
    // Use API key if available, otherwise attempt anonymous request (might fail for private repos)
    const authHeader = apiKey ? `Bearer ${apiKey}` : undefined;
    const headers = {
        'Accept': 'application/vnd.github.v3+json',
        ...(authHeader && {'Authorization': authHeader}) // Conditionally add Auth header
    };

    // Decide whether to use proxy or direct API call based on API key presence
    let fetchUrl = url;
    if (apiKey && url.startsWith('https://api.github.com')) {
         // Only use proxy if API key exists and it's a GitHub API URL
         // Note: The proxy URL itself might need adjustment depending on its requirements
         fetchUrl = `https://book.justlikemaki.vip/favorites/gtb?tk=${apiKey}&url=${encodeURIComponent(url)}`;
         // Proxy might inject CORS headers, client-side ones might not be needed/effective
         // headers['Access-Control-Allow-Origin'] = '*';
         // headers['Access-Control-Allow-Credentials'] = 'true';
         console.log("Using proxy for fetch:", fetchUrl);
    } else {
        console.log("Using direct API call (or API key missing):", fetchUrl);
    }


    showStatus(`${operationDesc}...`, false, true); // Use dynamic description

    try {
        const response = await fetch(fetchUrl, {
            method: 'GET',
            headers: headers
        });

        if (!response.ok) {
            let errorMsg = `Failed to fetch file (Status: ${response.status})`;
            try {
                const errorData = await response.json();
                errorMsg = `GitHub API Error (${response.status}): ${errorData.message || 'Failed to fetch file'}`;
            } catch (e) { /* Ignore if response is not JSON */ }
            throw new Error(errorMsg);
        }

        const data = await response.json();
        // Handle cases where the response from proxy might be different
        if(data.content === undefined || data.sha === undefined) {
             // Attempt to handle if proxy wraps the actual response
             if (data.data && data.data.content !== undefined && data.data.sha !== undefined) {
                 console.log("Adapting to proxy response structure.");
                 if (data.data.type !== 'file') {
                    throw new Error(`Path does not point to a file (points to ${data.data.type})`);
                 }
                 showStatus('File content fetched successfully (via proxy).', false, false);
                 return {
                     content: b64DecodeUnicode(data.data.content),
                     sha: data.data.sha
                 };
             } else {
                throw new Error("Invalid response format received (missing content/sha). Check proxy behavior.");
             }
        } else if (data.type !== 'file') { // Direct API response check
            throw new Error(`Path does not point to a file (points to ${data.type})`);
        }


        showStatus('File content fetched successfully.', false, false);
        return {
            content: b64DecodeUnicode(data.content),
            sha: data.sha
        };
    } catch (error) {
        showStatus(`Error fetching file: ${error.message}`, true);
        console.error("Fetch error:", error);
        return null;
    }
}

async function updateFileContent(owner, repo, path, newContent, sha, commitMessage) {
    const url = `https://api.github.com/repos/${owner}/${repo}/contents/${path}`;

    // Direct API call for PUT, proxy likely not needed/designed for this
     if (!apiKey) {
        showStatus(`Error updating file: GitHub API Key is required for updates.`, true);
        return null;
    }

    showStatus('Updating file content...', false, true);
    const encodedNewContent = b64EncodeUnicode(newContent);

    try {
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Accept': 'application/vnd.github.v3+json',
                'Authorization': `Bearer ${apiKey}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                message: commitMessage,
                content: encodedNewContent,
                sha: sha
            })
        });

        if (!response.ok) {
             let errorMsg = `Failed to update file (Status: ${response.status})`;
             try {
                 const errorData = await response.json();
                 const specificMessage = errorData.message || (response.status === 409 ? 'File has changed since fetch (conflict). Please retry.' : 'Failed to update file');
                 errorMsg = `GitHub API Error (${response.status}): ${specificMessage}`;
             } catch(e) { /* Ignore if response is not JSON */ }
             throw new Error(errorMsg);
        }

        const data = await response.json();
        const commitUrl = data.commit?.html_url;
        let successMsg = 'File updated successfully!';
        if (commitUrl) {
            successMsg += ` View commit: <a href="${commitUrl}" target="_blank">${commitUrl.split('/').pop().substring(0, 7)}</a>`;
            statusDiv.innerHTML = successMsg;
            statusDiv.className = 'success';
             // Ensure buttons are re-enabled by showStatus call below
             showStatus(successMsg, false, false); // Update status text without loading indicator
             statusDiv.innerHTML = successMsg; // Re-apply HTML link if showStatus overwrote it
             findLineButton.disabled = false; // Explicitly re-enable find button
             modifyButton.disabled = false; // Explicitly re-enable modify button

        } else {
             showStatus(successMsg, false, false);
        }

        return data;

    } catch (error) {
        showStatus(`Error updating file: ${error.message}`, true);
        console.error("Update error:", error);
        return null;
    }
}


// --- Main Logic ---

// Function to save current input values
function saveInputValues() {
    const valuesToSave = {
        owner: ownerInput.value,
        repo: repoInput.value,
        path: pathInput.value, 
        lineNumber: lineNumberInput.value,
        search: searchInput.value
    };
    // Save only non-empty fields we want to persist typically
    chrome.storage.local.set({ [STORAGE_KEY_LAST_INPUT]: valuesToSave }, () => {
        if (chrome.runtime.lastError) {
            console.error("Error saving input values:", chrome.runtime.lastError);
        } else {
            console.log('Saved last input values (excluding line number).');
        }
    });
}

// Function to load last input values and optionally populate from tab
function loadInputValues(callback) {
    chrome.storage.local.get([STORAGE_KEY_LAST_INPUT], (result) => {
        let loadedValues = {};
        if (chrome.runtime.lastError) {
             console.error("Error loading input values:", chrome.runtime.lastError);
        } else if (result[STORAGE_KEY_LAST_INPUT]) {
            loadedValues = result[STORAGE_KEY_LAST_INPUT];
            console.log('Loaded last input values from storage.');
        } else {
            console.log('No saved input values found in storage.');
        }

        ownerInput.value = loadedValues.owner || ownerInput.value || '';
        repoInput.value = loadedValues.repo || repoInput.value || '';
        pathInput.value = loadedValues.path || pathInput.value || '';
        lineNumberInput.value = loadedValues.lineNumber || lineNumberInput.value || '';
        searchInput.value = loadedValues.search || '';

        if (callback) {
            callback();
        }
    });
}

// Function to populate label and content from the active tab
async function populateFromActiveTab() {
    console.log("Querying active tab for default Label/Content...");
    try {
        const tabs = await chrome.tabs.query({ active: true, currentWindow: true });
        if (tabs && tabs.length > 0) {
            const currentTab = tabs[0];
            console.log("Active tab info:", currentTab);
            if (currentTab.title) {
                labelNameInput.value = currentTab.title;
                console.log("Populated Label from tab title.");
            }
            if (currentTab.url) {
                contentInput.value = currentTab.url;
                console.log("Populated Content from tab URL.");
            }
        }
    } catch (error) {
        console.error("Error getting active tab info:", error);
    }
}

// --- New Function: Find Line Number by Search Term ---
async function findAndSetLineNumber() {
    const owner = ownerInput.value.trim();
    const repo = repoInput.value.trim();
    const path = pathInput.value.trim();
    const searchTerm = searchInput.value.trim(); // Get search term

    if (!owner || !repo || !path) {
        showStatus("Error: Please fill in Owner, Repo, and Path before searching.", true);
        return;
    }
     if (!searchTerm) {
        showStatus("Tip: Enter text in 'Find Line Containing' field and click 'Find'.", false); // Changed to a tip/info message
        return;
    }
     if (!apiKey && !url.startsWith('https://api.github.com')) { // Check for API key needed for non-public repos potentially
         // We might need the API key even for fetching if repo is private. Let getFileContent handle it.
         console.warn("API Key not set, attempting fetch anyway (may fail for private repos).");
         // showStatus('Warning: GitHub API Key not set. Search might fail for private repos.', true);
         // return; // Or let it try
     }


    // 1. Fetch file content specifically for searching
    const fileData = await getFileContent(owner, repo, path, `Searching file for "${searchTerm}"`); // Pass description
    if (!fileData) {
        // Error already shown by getFileContent
        return;
    }

    // 2. Perform the search
    const lines = fileData.content.split('\n');
    let foundLine = -1; // Use -1 to indicate not found
    extractedLabelHint = '';

    for (let i = 0; i < lines.length; i++) {
        // Case-insensitive search is often more user-friendly
        if (lines[i].toLowerCase().includes(searchTerm.toLowerCase())) {
            foundLine = i + 1; // Found it - store 1-based index

            // --- Start: Extract text before '[' ---
            const matchedLineContent = lines[i]; // Get the full content of the matched line
            const bracketIndex = matchedLineContent.indexOf('['); // Find the index of the first '['

            if (bracketIndex !== -1) { // Check if '[' was found on this line
                // Extract the substring from the beginning (index 0) up to the bracket index
                // Use .trim() to remove leading/trailing whitespace, which is often desired for labels
                extractedLabelHint = matchedLineContent.substring(0, bracketIndex);

                console.log("Extracted text before '[':", extractedLabelHint); // Log for debugging

                // --- >>> Potential Use Case: Auto-fill Label Name <<< ---
                // Uncomment the line below if you want to automatically put this
                // extracted text into the "Label Name" input field.
                // labelNameInput.value = extractedLabelHint;
                // --- >>> End Potential Use Case <<< ---

            } else {
                // Handle case where the line matches the search term but doesn't contain '['
                extractedLabelHint = ''; // Ensure it's empty if no bracket found
                console.log("Search term matched, but '[' not found on line:", matchedLineContent);
                // Optionally clear the label input if bracket is expected but not found
                // labelNameInput.value = '';
            }
            // --- End: Extract text before '[' ---

            break; // Stop at the first match
        }
    }

    // 3. Update UI
    if (foundLine !== -1) {
        lineNumberInput.value = foundLine+1;
        showStatus(`Found "${searchTerm}" on line ${foundLine}. Ready to insert before it.`, false, false);
    } else {
        // Clear line number if search failed? Or leave it? Let's leave it for now.
        // lineNumberInput.value = '';
        lineNumberInput.value = -1;
        showStatus(`Text "${searchTerm}" not found in the file.`, true); // Show as error/warning
    }
}


// --- Modified handleModifyClick ---
async function handleModifyClick() {
    // --- Step 0: Confirmation ---
    // Get values needed for the confirmation message first
    const owner = ownerInput.value.trim();
    const repo = repoInput.value.trim();
    const path = pathInput.value.trim();
    const lineNumberStr = lineNumberInput.value.trim();
    const labelNameValue = labelNameInput.value; // Get potential label
    const contentValue = contentInput.value;     // Get potential content

    // Construct a descriptive confirmation message
    const confirmationMessage = `Confirm modification?\n\n` +
                                `Repo: ${owner}/${repo || '?'}\n` +
                                `File: ${path || '?'}\n` +
                                `Line: ${lineNumberStr || '?'}\n` +
                                `Content Label: ${labelNameValue.substring(0, 30)}${labelNameValue.length > 30 ? '...' : ''}\n\n` + // Show preview
                                `This will insert content and commit changes to GitHub.`;

    // Show the confirmation dialog
    const proceed = window.confirm(confirmationMessage);

    // If user clicks "Cancel", stop the function
    if (!proceed) {
        showStatus("Modification cancelled by user.", false, false); // Update status
        // Ensure buttons are enabled as showStatus might have been called with isLoading=true before this point if find was running
        findLineButton.disabled = false;
        modifyButton.disabled = false;
        return; // Exit the function
    }

    // --- User confirmed, proceed with the original logic ---

    // 1. Validate Inputs (some already retrieved, re-validate trimmed)
    if (!owner || !repo || !path || !lineNumberStr) {
        showStatus("Error: Please fill in Owner, Repo, Path, and Line Number.", true);
        return;
    }

    const lineNumber = parseInt(lineNumberStr, 10);
    if (isNaN(lineNumber) || lineNumber < 1) {
        showStatus("Error: Line number must be a positive integer.", true);
        return;
    }

    if (!apiKey) {
         showStatus('Error: GitHub API Key not set. Please configure it in the extension options.', true);
         return;
    }

    // Status updates below will handle disabling buttons during API calls
    // showStatus("Proceeding with modification...", false, true); // Optional: Indicate confirmation received

    // 2. Get current file content and SHA
    const fileData = await getFileContent(owner, repo, path, 'Fetching latest file version');
    if (!fileData) {
        return; // Error shown by getFileContent
    }

    // 3. Modify the content
    const contentToAdd = `${extractedLabelHint}[${labelNameValue}](${contentValue})`; // Construct final content string
    const lines = fileData.content.split('\n');
    const targetIndex = lineNumber - 1;

     if (targetIndex < 0 || targetIndex > lines.length) {
        showStatus(`Error: Line number ${lineNumber} is out of range for the current file version (File has ${lines.length} lines). Max allowed: ${lines.length + 1}. Please 'Find Line' again or correct the number.`, true);
        return;
    }

    lines.splice(targetIndex, 0, contentToAdd);
    const newContent = lines.join('\n');

    // 4. Update the file on GitHub
    const commitMessage = `Add content via Chrome Extension at line ${lineNumber} in ${path}`;
    const updateResult = await updateFileContent(owner, repo, path, newContent, fileData.sha, commitMessage);

    // 5. Save inputs on successful update
    if (updateResult) {
       saveInputValues();
       // Optional: Clear fields after success
       // lineNumberInput.value = '';
       // searchInput.value = '';
       // labelNameInput.value = '';
       // contentInput.value = '';
    }
    // Button state managed by showStatus calls within API functions / final success message
}

// --- Initialization ---
function initializePopup() {
    chrome.storage.local.get(['githubApiKey'], (result) => {
        if (result.githubApiKey) {
            apiKey = result.githubApiKey;
            // Don't show API status immediately, let fetch/update handle it
            // showStatus('API Key loaded. Ready.', false, false);
        } else {
            showStatus('Warning: GitHub API Key not set. Please configure it in the options.', true);
        }
        loadInputValues(() => {
            populateFromActiveTab();
             // Initial status message after loading everything
             if (!apiKey) {
                 showStatus('Warning: GitHub API Key not set. Please configure it in the options.', true);
             } else if (statusDiv.textContent === '' || statusDiv.textContent.includes('Warning:')) { // Only set ready if no warning/error shown
                 showStatus('Ready. Fill fields or find line.', false, false);
             }
        });
    });

    modifyButton.addEventListener('click', handleModifyClick);
    findLineButton.addEventListener('click', findAndSetLineNumber); // Add listener for the find button
}

document.addEventListener('DOMContentLoaded', initializePopup);

// --- END OF FILE popup.js ---