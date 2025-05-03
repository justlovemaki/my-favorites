// --- START OF FILE popup.js ---

// Input Elements
const ownerInput = document.getElementById('owner');
const repoInput = document.getElementById('repo');
const pathInput = document.getElementById('path');
const searchInput = document.getElementById('searchInput');
const lineNumberInput = document.getElementById('lineNumber');
const labelNameInput = document.getElementById('labelName'); 
const contentInput = document.getElementById('content');      

// Button Elements
const findLineButton = document.getElementById('findLineButton');
const insertButton = document.getElementById('insertButton'); 
const editButton = document.getElementById('editButton');    
const deleteButton = document.getElementById('deleteButton');  

// Other Elements
const statusDiv = document.getElementById('status');

let extractedLabelHint = ''; // Still used for Insert formatting
let apiKey = null;
const STORAGE_KEY_LAST_INPUT = 'lastInputValues';

// --- Helper Functions ---

// Display status messages (disables all action buttons when loading)
function showStatus(message, isError = false, isLoading = false) {
    statusDiv.textContent = message;
    statusDiv.className = isError ? 'error' : (isLoading ? '' : 'success');

    const disableButtons = isLoading;
    findLineButton.disabled = disableButtons;
    insertButton.disabled = disableButtons;
    editButton.disabled = disableButtons;
    deleteButton.disabled = disableButtons;

    // Re-enable specifically if not loading
    if (!isLoading) {
        const isSuccessWithLink = !isError && statusDiv.className === 'success' && statusDiv.innerHTML.includes('</a>');
        // Re-enable all buttons after operation, unless it's a success with a link (where specific logic might keep them disabled until user action)
        // Let's generally re-enable them unless specifically told otherwise.
        findLineButton.disabled = false;
        insertButton.disabled = false;
        editButton.disabled = false;
        deleteButton.disabled = false;
    }
    // Special handling for success messages with links (from updateFileContent)
    if (!isLoading && statusDiv.innerHTML.includes('</a>')) {
        // Re-apply HTML link if showStatus overwrote it (textContent sets plain text)
        statusDiv.innerHTML = message;
        // Re-enable buttons explicitly after potential link message
        findLineButton.disabled = false;
        insertButton.disabled = false;
        editButton.disabled = false;
        deleteButton.disabled = false;
    }
}


// Base64 encode (UTF-8 safe)
function b64EncodeUnicode(str) {
    // Replacing '+' with '-' and '/' with '_' makes it URL-safe, but GitHub API expects standard Base64
    // Using btoa directly after encodeURIComponent is standard
    try {
        return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g,
            function toSolidBytes(match, p1) {
                return String.fromCharCode('0x' + p1);
        }));
    } catch (e) {
        console.error("Base64 Encoding Error:", e);
        showStatus("Error: Could not encode content for GitHub.", true);
        return null; // Return null on error
    }
}

// Base64 decode (UTF-8 safe)
function b64DecodeUnicode(str) {
    try {
        // Standard Base64 decoding
        return decodeURIComponent(atob(str).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
    } catch(e) {
        console.error("Base64 Decoding Error:", e);
        showStatus("Error: Could not decode file content from GitHub.", true);
        return null; // Return null on error
    }
}

// --- GitHub API Interaction ---
async function getFileContent(owner, repo, path, operationDesc = 'Fetching file content') {
    const url = `https://api.github.com/repos/${owner}/${repo}/contents/${path}`;
    const authHeader = apiKey ? `Bearer ${apiKey}` : undefined;
    const headers = {
        'Accept': 'application/vnd.github.v3+json',
        ...(authHeader && {'Authorization': authHeader})
    };

    let fetchUrl = url;
    // Using a proxy? Adjust as needed. This example assumes direct API calls or a specific proxy setup.
    // if (apiKey && url.startsWith('https://api.github.com')) {
    //      fetchUrl = `YOUR_PROXY_URL?tk=${apiKey}&url=${encodeURIComponent(url)}`;
    //      console.log("Using proxy for fetch:", fetchUrl);
    // } else {
    //     console.log("Using direct API call (or API key missing):", fetchUrl);
    // }
    console.log("Attempting to fetch:", fetchUrl); // Log the URL being fetched

    showStatus(`${operationDesc}...`, false, true);

    try {
        const response = await fetch(fetchUrl, {
            method: 'GET',
            headers: headers,
            cache: 'no-cache' // Try to prevent caching issues
        });

        const responseBody = await response.text(); // Read body once
        // console.log("Raw response body:", responseBody); // Debug: Log raw response

        if (!response.ok) {
            let errorMsg = `Failed to fetch file (Status: ${response.status} ${response.statusText})`;
            try {
                const errorData = JSON.parse(responseBody); // Try parsing the body we read
                errorMsg = `GitHub API Error (${response.status}): ${errorData.message || 'Failed to fetch file'}`;
                 if (response.status === 404) {
                    errorMsg += ` - Check Owner, Repo, and Path.`;
                } else if (response.status === 403) {
                     errorMsg += ` - Check API Key permissions or rate limits.`;
                }
            } catch (e) {
                console.warn("Response body was not JSON:", responseBody);
             }
            throw new Error(errorMsg);
        }

        const data = JSON.parse(responseBody); // Parse the body we read

        // Handle cases where the response might be unexpected (e.g., proxy wrapping)
        let fileContentBase64 = data.content;
        let fileSha = data.sha;
        let fileType = data.type;

        // Basic check for proxy structure (adapt if your proxy is different)
        if (fileContentBase64 === undefined && data.data && data.data.content !== undefined) {
             console.log("Adapting to potential proxy response structure.");
             fileContentBase64 = data.data.content;
             fileSha = data.data.sha;
             fileType = data.data.type;
        }


        if (fileContentBase64 === undefined || fileSha === undefined) {
             console.error("Invalid response format:", data); // Log the problematic data
             throw new Error("Invalid response format received (missing content/sha).");
        }
        if (fileType !== 'file') {
            throw new Error(`Path does not point to a file (points to ${fileType || 'unknown type'})`);
        }

        const decodedContent = b64DecodeUnicode(fileContentBase64);
        if (decodedContent === null) { // Check if decoding failed
             throw new Error("Failed to decode file content from Base64.");
        }

        showStatus('File content fetched successfully.', false, false);
        return {
            content: decodedContent,
            sha: fileSha
        };
    } catch (error) {
        showStatus(`Error fetching file: ${error.message}`, true);
        console.error("Fetch error details:", error);
        return null;
    }
}

async function updateFileContent(owner, repo, path, newContent, sha, commitMessage) {
    const url = `https://api.github.com/repos/${owner}/${repo}/contents/${path}`;

     if (!apiKey) {
        showStatus(`Error updating file: GitHub API Key is required for updates.`, true);
        return null;
    }

    showStatus('Updating file content...', false, true);
    const encodedNewContent = b64EncodeUnicode(newContent);
     if (encodedNewContent === null) { // Check if encoding failed
        // showStatus already called by b64EncodeUnicode on error
        return null;
    }


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
                sha: sha // Must include the SHA of the file being updated
            })
        });

         const responseBody = await response.text(); // Read body once
         // console.log("Raw update response body:", responseBody); // Debug log

        if (!response.ok) {
             let errorMsg = `Failed to update file (Status: ${response.status} ${response.statusText})`;
             try {
                 const errorData = JSON.parse(responseBody);
                 const specificMessage = errorData.message || (response.status === 409 ? 'File has changed since fetch (conflict). Please retry.' : 'Failed to update file');
                 errorMsg = `GitHub API Error (${response.status}): ${specificMessage}`;
                  if (response.status === 401) errorMsg += ' - Invalid API Key?';
                  if (response.status === 403) errorMsg += ' - API Key lacks write permission?';
                  if (response.status === 422 && specificMessage.includes('sha')) errorMsg += ' - SHA mismatch likely (file changed).';

             } catch(e) {  console.warn("Update response body was not JSON:", responseBody); }
             throw new Error(errorMsg);
        }

        const data = JSON.parse(responseBody);
        const commitUrl = data.commit?.html_url;
        let successMsg = 'File updated successfully!';
        if (commitUrl) {
            const shortSha = commitUrl.split('/').pop().substring(0, 7);
            successMsg += ` View commit: <a href="${commitUrl}" target="_blank">${shortSha}</a>`;
            // Call showStatus first to handle button states, then set innerHTML
            showStatus(successMsg, false, false);
            statusDiv.innerHTML = successMsg; // Set link HTML
        } else {
             showStatus(successMsg, false, false);
        }

        return data;

    } catch (error) {
        showStatus(`Error updating file: ${error.message}`, true);
        console.error("Update error details:", error);
        return null;
    }
}

// --- Input/Storage ---

// Function to save current input values (repo details)
function saveInputValues() {
    const valuesToSave = {
        owner: ownerInput.value,
        repo: repoInput.value,
        path: pathInput.value,
        lineNumber: lineNumberInput.value,
        search: searchInput.value
    };
    chrome.storage.local.set({ [STORAGE_KEY_LAST_INPUT]: valuesToSave }, () => {
        if (chrome.runtime.lastError) {
            console.error("Error saving input values:", chrome.runtime.lastError);
        } else {
            console.log('Saved last repo input values.');
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
            ownerInput.value = loadedValues.owner || '';
            repoInput.value = loadedValues.repo || '';
            pathInput.value = loadedValues.path || '';
            lineNumberInput.value = loadedValues.lineNumber || '';
            searchInput.value = loadedValues.search || '';
        } else {
            console.log('No saved input values found in storage.');
        }
        if (callback) {
            callback(); // Proceed even if loading failed or no data
        }
    });
}


// Function to populate label and content for INSERT from the active tab
async function populateFromActiveTab() {
    console.log("Querying active tab for default Insert fields...");
    try {
        // Use chrome.tabs.query with async/await
        const tabs = await chrome.tabs.query({ active: true, currentWindow: true });
        if (tabs && tabs.length > 0) {
            const currentTab = tabs[0];
            console.log("Active tab info:", currentTab);
             // Only populate if the fields are currently empty
            if (currentTab.title && !labelNameInput.value) {
                labelNameInput.value = currentTab.title;
                console.log("Populated Label Name (for Insert) from tab title.");
            }
            if (currentTab.url && !contentInput.value) {
                 // Avoid chrome:// URLs
                 if (!currentTab.url.startsWith('chrome://')) {
                    contentInput.value = currentTab.url;
                    console.log("Populated Content URL (for Insert) from tab URL.");
                 } else {
                     console.log("Skipped populating Content URL from chrome:// URL.");
                 }
            }
        } else {
             console.log("No active tab found or insufficient permissions.");
        }
    } catch (error) {
        console.error("Error getting active tab info:", error);
         // Attempt to inform user in status if needed, but console error is primary
         // showStatus("Could not access tab details. Check permissions?", true);
    }
}


// --- Action Handlers ---

// Find Line Number by Search Term
async function findAndSetLineNumber() {
    const owner = ownerInput.value.trim();
    const repo = repoInput.value.trim();
    const path = pathInput.value.trim();
    const searchTerm = searchInput.value.trim();

    if (!owner || !repo || !path) {
        showStatus("Error: Please fill in Owner, Repo, and Path before searching.", true);
        return;
    }
    if (!searchTerm) {
        showStatus("Tip: Enter text in 'Find Line Containing' field and click 'Find'.", false);
        return;
    }

    // Check for API key - needed for private repos even for GET
    if (!apiKey) {
         console.warn("API Key not set, attempting fetch anyway (may fail for private repos).");
         // Optionally show a less severe warning
         // showStatus('Warning: GitHub API Key not set. Search might fail for private repos.', false);
     }

    const fileData = await getFileContent(owner, repo, path, `Searching file for "${searchTerm}"`);
    if (!fileData) return; // Error shown by getFileContent

    const lines = fileData.content.split('\n');
    let foundLine = -1;
    extractedLabelHint = ''; // Reset hint

    for (let i = 0; i < lines.length; i++) {
        if (lines[i].toLowerCase().includes(searchTerm.toLowerCase())) {
            foundLine = i + 1; // 1-based index

            // Extract text before '[' for potential Insert label hint
            const bracketIndex = lines[i].indexOf('[');
            if (bracketIndex !== -1) {
                extractedLabelHint = lines[i].substring(0, bracketIndex);
                 // Do NOT auto-fill labelNameInput here - keep it manual or based on tab title.
                 // This hint is ONLY used if Insert is clicked *after* a successful Find.
                console.log("Extracted label hint for Insert:", extractedLabelHint);
            } else {
                 extractedLabelHint = ''; // Reset if no bracket found on matched line
            }
            break; // Stop at first match
        }
    }

    if (foundLine !== -1) {
        lineNumberInput.value = foundLine;
        showStatus(`Found "${searchTerm}" on line ${foundLine}. Target line set.`, false, false);
         // You could optionally populate the 'Edit Content' field with the found line's content
    } else {
        lineNumberInput.value = ''; // Clear line number if not found
        showStatus(`Text "${searchTerm}" not found in the file.`, true);
        extractedLabelHint = ''; // Clear hint if search failed
    }
}


// Generic function to handle pre-action checks and confirmation
async function prepareAndConfirm(actionType) {
    // 1. Get common values & Validate Inputs
    const owner = ownerInput.value.trim();
    const repo = repoInput.value.trim();
    const path = pathInput.value.trim();
    const lineNumberStr = lineNumberInput.value.trim();

    if (!owner || !repo || !path || !lineNumberStr) {
        showStatus("Error: Please fill in Owner, Repo, Path, and Target Line Number.", true);
        return null; // Indicate failure
    }

    const lineNumber = parseInt(lineNumberStr, 10);
    if (isNaN(lineNumber) || lineNumber < 1) {
        showStatus("Error: Target Line Number must be a positive integer.", true);
        return null; // Indicate failure
    }

     // 1b. API Key Check (Essential for PUT/DELETE)
     if (!apiKey) {
         showStatus('Error: GitHub API Key not set. Please configure it in the extension options.', true);
         return null; // Indicate failure
     }

    // 2. Action-Specific Validation & Confirmation Message
    let confirmationMessage = `Confirm ${actionType} Action?\n\n` +
                              `Repo: ${owner}/${repo}\n` +
                              `File: ${path}\n` +
                              `Line: ${lineNumber}\n\n`;
    let actionSpecificData = {};

    const labelNameValue = labelNameInput.value; // No trim needed? User might want spaces
    const contentValue = contentInput.value.trim(); // Trim URL
    if (actionType === 'Insert') {
        if (!contentValue) { // Content URL is essential for insert format
            showStatus("Error: Content URL (for Insert) cannot be empty.", true);
            return null;
        }
        // Construct the line to be inserted for confirmation preview
        // Use extractedLabelHint if available AND labelNameInput is empty
        console.log("extractedLabelHint: "+extractedLabelHint);
        const finalLabelHint = (extractedLabelHint) ? extractedLabelHint : '';
        const lineToInsert = `${finalLabelHint}[${labelNameValue}](${contentValue})`;
        confirmationMessage += `Content to Insert:\n${lineToInsert.substring(0, 100)}${lineToInsert.length > 100 ? '...' : ''}\n\nThis will insert the line and commit changes.`;
        actionSpecificData = { labelName: labelNameValue, contentUrl: contentValue, insertContent: lineToInsert };

    } else if (actionType === 'Edit') {
        const finalLabelHint = (extractedLabelHint) ? extractedLabelHint : '';
        const lineToEdit = `${finalLabelHint}[${labelNameValue}](${contentValue})`;
        // Allow empty string for edit (effectively clearing the line content)
        confirmationMessage += `New Line Content:\n${lineToEdit.substring(0, 100)}${lineToEdit.length > 100 ? '...' : ''}\n\nThis will replace the line content and commit changes.`;
        actionSpecificData = { newContent: lineToEdit };

    } else if (actionType === 'Delete') {
        confirmationMessage += `This will permanently delete the line and commit changes.`;
        // No specific data needed for delete besides line number

    } else {
        showStatus("Error: Unknown action type.", true);
        return null;
    }

    // 3. Show Confirmation Dialog
    const proceed = window.confirm(confirmationMessage);
    if (!proceed) {
        showStatus(`${actionType} cancelled by user.`, false, false);
        return null; // Indicate cancellation
    }

    // 4. Return necessary data if confirmed
    return {
        owner,
        repo,
        path,
        lineNumber, // Return the parsed number
        actionSpecificData // Return data needed for the specific action
    };
}

// --- Action Handlers ---

// Handle INSERT button click
async function handleInsertClick() {
    const prepData = await prepareAndConfirm('Insert');
    if (!prepData) return; // Exit if validation failed or user cancelled

    const { owner, repo, path, lineNumber, actionSpecificData } = prepData;
    const { insertContent } = actionSpecificData;

    // Fetch latest file content
    const fileData = await getFileContent(owner, repo, path, 'Fetching latest file version for Insert');
    if (!fileData) return; // Error shown by getFileContent

    // Modify the content
    const contentToAdd = insertContent;
    const lines = fileData.content.split('\n');
    const targetIndex = lineNumber - 0; // 0-based index

     // Validate line number against actual file length AFTER fetching
     if (targetIndex < 0 || targetIndex > lines.length) { // Allow inserting at the very end (index == lines.length)
        showStatus(`Error: Line number ${lineNumber} is out of range. File has ${lines.length} lines. Allowed range: 1 to ${lines.length + 1}.`, true);
        return;
    }

    lines.splice(targetIndex, 0, contentToAdd); // Insert the new line
    const newContent = lines.join('\n');

    // Update the file on GitHub
    const commitMessage = `Chrome Extension: Insert line at ${lineNumber} in ${path}`;
    const updateResult = await updateFileContent(owner, repo, path, newContent, fileData.sha, commitMessage);

    // Save inputs on successful update
    if (updateResult) {
       saveInputValues(); // Save owner/repo/path
       // Optional: Clear insert-specific fields after success
       // labelNameInput.value = '';
       // contentInput.value = '';
       // lineNumberInput.value = ''; // Maybe clear line number?
       // searchInput.value = '';
       // extractedLabelHint = '';
    }
}

// Handle EDIT button click
async function handleEditClick() {
    const prepData = await prepareAndConfirm('Edit');
    if (!prepData) return; // Exit if validation failed or user cancelled

    const { owner, repo, path, lineNumber, actionSpecificData } = prepData;
    const { newContent: newLineEditContent } = actionSpecificData; // Renamed for clarity

    // Fetch latest file content
    const fileData = await getFileContent(owner, repo, path, 'Fetching latest file version for Edit');
    if (!fileData) return; // Error shown by getFileContent

    // Modify the content
    const lines = fileData.content.split('\n');
    const targetIndex = lineNumber - 1; // 0-based index

    // Validate line number against actual file length AFTER fetching
    if (targetIndex < 0 || targetIndex >= lines.length) { // Must be an existing line index
        showStatus(`Error: Line number ${lineNumber} is out of range. File has ${lines.length} lines. Cannot edit non-existent line.`, true);
        return;
    }

    const originalLine = lines[targetIndex]; // Keep original for commit message potentially
    console.log(`Editing line ${lineNumber}: "${originalLine}"`); // Log original content
    lines[targetIndex] = newLineEditContent; // Replace the line
    const newContent = lines.join('\n');

    // Update the file on GitHub
    const commitMessage = `Chrome Extension: Edit line ${lineNumber} in ${path}`;
    const updateResult = await updateFileContent(owner, repo, path, newContent, fileData.sha, commitMessage);

     // Save inputs on successful update
    if (updateResult) {
       saveInputValues(); // Save owner/repo/path
        // Optional: Clear edit-specific field after success
        // lineNumberInput.value = '';
        // searchInput.value = '';
        // extractedLabelHint = '';
    }
}

// Handle DELETE button click
async function handleDeleteClick() {
    const prepData = await prepareAndConfirm('Delete');
    if (!prepData) return; // Exit if validation failed or user cancelled

    const { owner, repo, path, lineNumber } = prepData;
    // No actionSpecificData needed for delete

    // Fetch latest file content
    const fileData = await getFileContent(owner, repo, path, 'Fetching latest file version for Delete');
    if (!fileData) return; // Error shown by getFileContent

    // Modify the content
    const lines = fileData.content.split('\n');
    const targetIndex = lineNumber - 1; // 0-based index

    // Validate line number against actual file length AFTER fetching
    if (targetIndex < 0 || targetIndex >= lines.length) { // Must be an existing line index
        showStatus(`Error: Line number ${lineNumber} is out of range. File has ${lines.length} lines. Cannot delete non-existent line.`, true);
        return;
    }

    const deletedLine = lines.splice(targetIndex, 1)[0]; // Remove the line and get its content
    const newContent = lines.join('\n');
    console.log(`Deleted line ${lineNumber}: "${deletedLine}"`); // Log deleted content

    // Update the file on GitHub
    const commitMessage = `Chrome Extension: Delete line ${lineNumber} in ${path}`;
    const updateResult = await updateFileContent(owner, repo, path, newContent, fileData.sha, commitMessage);

     // Save inputs on successful update
    if (updateResult) {
       saveInputValues(); // Save owner/repo/path
        // Optional: Clear fields after success
        // lineNumberInput.value = '';
        // searchInput.value = '';
        // extractedLabelHint = '';
    }
}


// --- Initialization ---
function initializePopup() {
    chrome.storage.local.get(['githubApiKey'], (result) => {
        if (result.githubApiKey) {
            apiKey = result.githubApiKey;
            console.log("API Key loaded.");
        } else {
            // Show initial warning, but allow proceeding for public repo reads/searches
            showStatus('Warning: GitHub API Key not set. Updates/Edits/Deletes and private repo access will fail. Configure in options.', true);
            // Keep buttons enabled initially for potential public repo reads
             findLineButton.disabled = false;
             insertButton.disabled = false; // Will fail later if key needed
             editButton.disabled = false; // Will fail later if key needed
             deleteButton.disabled = false; // Will fail later if key needed
        }

        // Load saved repo details, then populate tab details
        loadInputValues(() => {
            populateFromActiveTab().then(() => {
                // Set initial ready status only if no warning is currently shown
                if (statusDiv.className !== 'error' && !statusDiv.textContent.startsWith('Warning:')) {
                    showStatus('Ready. Fill fields or find line.', false, false);
                }
            });
        });
    });

    // Add listeners for all buttons
    findLineButton.addEventListener('click', findAndSetLineNumber);
    insertButton.addEventListener('click', handleInsertClick);
    editButton.addEventListener('click', handleEditClick);
    deleteButton.addEventListener('click', handleDeleteClick);

    // Add listeners to save repo inputs on change (optional, could be annoying)
    // ownerInput.addEventListener('change', saveInputValues);
    // repoInput.addEventListener('change', saveInputValues);
    // pathInput.addEventListener('change', saveInputValues);
}

// Use DOMContentLoaded to ensure HTML is parsed before JS runs
document.addEventListener('DOMContentLoaded', initializePopup);

// --- END OF FILE popup.js ---
