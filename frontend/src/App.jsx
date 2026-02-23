import { useEffect, useMemo, useState } from "react";
import { api, authApi } from "./api";

const initialOwner = { firstName: "", lastName: "", email: "", phoneNumber: "", address: "", username: "", password: "" };
const initialAnimal = {
  animalId: "",
  color: "",
  dateOfBirth: "",
  breed: "",
  type: "CATTLE",
  image: "",
  parentId: "",
  ownerId: ""
};

function parseIds(value) {
  return value.split(",").map((x) => Number(x.trim())).filter(Boolean);
}

function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

function LoginPage({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const demoUsers = "Default admin (from backend env): admin / admin123";

  async function submit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const session = await authApi.login(username, password);
      onLogin(session);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page narrow">
      <header className="hero">
        <h1>Animal Farm Login</h1>
        <p>Sign in to continue.</p>
      </header>
      <form className="card login" onSubmit={submit}>
        <h2>Login</h2>
        <input placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} required />
        <input placeholder="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <button type="submit" disabled={loading}>{loading ? "Signing in..." : "Sign in"}</button>
        <small>Demo users: {demoUsers}</small>
      </form>
      {error && <div className="notice error">{error}</div>}
    </div>
  );
}

function ForcePasswordResetPage({ session, onPasswordChanged, onLogout }) {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setError("");
    setMessage("");
    if (newPassword !== confirmPassword) {
      setError("New password and confirm password do not match.");
      return;
    }
    setLoading(true);
    try {
      const updatedSession = await authApi.changePassword(currentPassword, newPassword);
      setMessage("Password changed successfully.");
      onPasswordChanged(updatedSession);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page narrow">
      <header className="hero">
        <h1>Password Reset Required</h1>
        <p>{session.username}, update your temporary password to continue.</p>
      </header>
      <form className="card login" onSubmit={submit}>
        <h2>Change Password</h2>
        <input type="password" placeholder="Current password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required />
        <input type="password" placeholder="New password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required />
        <input type="password" placeholder="Confirm new password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />
        <button type="submit" disabled={loading}>{loading ? "Saving..." : "Save Password"}</button>
        <button type="button" onClick={onLogout}>Logout</button>
      </form>
      {message && <div className="notice">{message}</div>}
      {error && <div className="notice error">{error}</div>}
    </div>
  );
}

function AdminPage({ session, onLogout }) {
  const [ownerForm, setOwnerForm] = useState(initialOwner);
  const [animalForm, setAnimalForm] = useState(initialAnimal);
  const [transferForm, setTransferForm] = useState({ toOwnerId: "", animalIds: "" });
  const [ownerSearch, setOwnerSearch] = useState({ ownerId: "", firstName: "" });
  const [ownerResults, setOwnerResults] = useState([]);
  const [editingOwner, setEditingOwner] = useState(null);
  const [reportForm, setReportForm] = useState({ ownerId: "", parentId: "" });
  const [animals, setAnimals] = useState([]);
  const [transferRequests, setTransferRequests] = useState([]);
  const [message, setMessage] = useState("");

  const refresh = async () => {
    const [animalList, trs] = await Promise.all([api.getAnimals(), api.listTransferRequests()]);
    setAnimals(animalList);
    setTransferRequests(trs);
  };

  useEffect(() => {
    refresh().catch((e) => setMessage(e.message));
  }, []);

  async function onCreateOwner(e) {
    e.preventDefault();
    try {
      await api.registerOwner(ownerForm);
      setOwnerForm(initialOwner);
      setMessage("Owner registered.");
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function onCreateAnimal(e) {
    e.preventDefault();
    try {
      await api.registerAnimal({
        ...animalForm,
        ownerId: Number(animalForm.ownerId),
        parentId: animalForm.parentId ? Number(animalForm.parentId) : null
      });
      setAnimalForm(initialAnimal);
      await refresh();
      setMessage("Animal registered.");
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function onTransfer(e) {
    e.preventDefault();
    try {
      await api.transferAnimals({
        toOwnerId: Number(transferForm.toOwnerId),
        animalIds: parseIds(transferForm.animalIds)
      });
      setTransferForm({ toOwnerId: "", animalIds: "" });
      await refresh();
      setMessage("Transfer completed.");
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function onSearchOwners(e) {
    e.preventDefault();
    try {
      const results = await api.searchOwners({
        ownerId: ownerSearch.ownerId.trim(),
        firstName: ownerSearch.firstName.trim()
      });
      setOwnerResults(results);
      if (results.length === 0) {
        setMessage("No owners found.");
      } else {
        setMessage(`Found ${results.length} owner(s).`);
      }
    } catch (err) {
      setMessage(err.message);
    }
  }

  function startEditOwner(owner) {
    setEditingOwner({
      id: owner.id,
      username: owner.username || "",
      hasCredentials: owner.credentialsCreated,
      firstName: owner.firstName,
      lastName: owner.lastName,
      email: owner.email,
      phoneNumber: owner.phoneNumber,
      address: owner.address,
      temporaryPassword: ""
    });
  }

  async function onSaveOwner(e) {
    e.preventDefault();
    if (!editingOwner) return;
    try {
      await api.updateOwner(editingOwner.id, {
        firstName: editingOwner.firstName,
        lastName: editingOwner.lastName,
        email: editingOwner.email,
        phoneNumber: editingOwner.phoneNumber,
        address: editingOwner.address,
        username: editingOwner.username || null,
        temporaryPassword: editingOwner.temporaryPassword || null
      });
      const results = await api.searchOwners({
        ownerId: ownerSearch.ownerId.trim(),
        firstName: ownerSearch.firstName.trim()
      });
      setOwnerResults(results);
      setMessage("Owner updated successfully.");
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function onReport(type, value, filename) {
    try {
      const blob = await api.downloadReport(type, value);
      downloadBlob(blob, filename);
    } catch (err) {
      setMessage(err.message);
    }
  }

  return (
    <div className="page">
      <header className="hero topbar">
        <div>
          <h1>Admin Dashboard</h1>
          <p>{session.username} ({session.role})</p>
        </div>
        <button onClick={onLogout}>Logout</button>
      </header>
      {message && <div className="notice">{message}</div>}

      <section className="grid">
        <form className="card" onSubmit={onCreateOwner}>
          <h2>Register Owner</h2>
          <input placeholder="First name" value={ownerForm.firstName} onChange={(e) => setOwnerForm({ ...ownerForm, firstName: e.target.value })} required />
          <input placeholder="Last name" value={ownerForm.lastName} onChange={(e) => setOwnerForm({ ...ownerForm, lastName: e.target.value })} required />
          <input placeholder="Email" type="email" value={ownerForm.email} onChange={(e) => setOwnerForm({ ...ownerForm, email: e.target.value })} required />
          <input placeholder="Phone" value={ownerForm.phoneNumber} onChange={(e) => setOwnerForm({ ...ownerForm, phoneNumber: e.target.value })} required />
          <input placeholder="Address" value={ownerForm.address} onChange={(e) => setOwnerForm({ ...ownerForm, address: e.target.value })} required />
          <input placeholder="Username" value={ownerForm.username} onChange={(e) => setOwnerForm({ ...ownerForm, username: e.target.value })} required />
          <input placeholder="Temporary password" type="password" value={ownerForm.password} onChange={(e) => setOwnerForm({ ...ownerForm, password: e.target.value })} required />
          <button type="submit">Create Owner</button>
        </form>

        <form className="card" onSubmit={onCreateAnimal}>
          <h2>Register Animal</h2>
          <input placeholder="Animal ID" value={animalForm.animalId} onChange={(e) => setAnimalForm({ ...animalForm, animalId: e.target.value })} required />
          <input placeholder="Color" value={animalForm.color} onChange={(e) => setAnimalForm({ ...animalForm, color: e.target.value })} required />
          <input type="date" value={animalForm.dateOfBirth} onChange={(e) => setAnimalForm({ ...animalForm, dateOfBirth: e.target.value })} required />
          <input placeholder="Breed" value={animalForm.breed} onChange={(e) => setAnimalForm({ ...animalForm, breed: e.target.value })} required />
          <select value={animalForm.type} onChange={(e) => setAnimalForm({ ...animalForm, type: e.target.value })}>
            <option value="CATTLE">CATTLE</option>
            <option value="GOAT">GOAT</option>
            <option value="RAM">RAM</option>
            <option value="PIG">PIG</option>
          </select>
          <input placeholder="Image URL" value={animalForm.image} onChange={(e) => setAnimalForm({ ...animalForm, image: e.target.value })} />
          <input placeholder="Parent Animal DB Id (optional)" value={animalForm.parentId} onChange={(e) => setAnimalForm({ ...animalForm, parentId: e.target.value })} />
          <input placeholder="Owner ID" value={animalForm.ownerId} onChange={(e) => setAnimalForm({ ...animalForm, ownerId: e.target.value })} required />
          <button type="submit">Create Animal</button>
        </form>

        <form className="card" onSubmit={onTransfer}>
          <h2>Transfer Animals</h2>
          <input placeholder="To Owner ID" value={transferForm.toOwnerId} onChange={(e) => setTransferForm({ ...transferForm, toOwnerId: e.target.value })} required />
          <input placeholder="Animal DB IDs (comma-separated)" value={transferForm.animalIds} onChange={(e) => setTransferForm({ ...transferForm, animalIds: e.target.value })} required />
          <button type="submit">Transfer</button>
        </form>

        <div className="card">
          <h2>Admin Reports</h2>
          <button type="button" onClick={() => onReport("ownersList", "", "owners-list.pdf")}>Owners List</button>
          <button type="button" onClick={() => onReport("ownersAnimalTypeCounts", "", "owners-animal-type-counts.pdf")}>Owners Animal Counts</button>
          <input placeholder="Owner ID" value={reportForm.ownerId} onChange={(e) => setReportForm({ ...reportForm, ownerId: e.target.value })} />
          <button type="button" onClick={() => onReport("ownerVsAnimal", reportForm.ownerId, `owner-vs-animal-${reportForm.ownerId}.pdf`)}>Owner vs Animal</button>
          <button type="button" onClick={() => onReport("owner", reportForm.ownerId, `owner-animal-${reportForm.ownerId}.pdf`)}>Owner Animal</button>
          <input placeholder="Parent Animal DB ID" value={reportForm.parentId} onChange={(e) => setReportForm({ ...reportForm, parentId: e.target.value })} />
          <button type="button" onClick={() => onReport("parentVsAnimal", reportForm.parentId, `parent-vs-animal-${reportForm.parentId}.pdf`)}>Parent vs Animal</button>
        </div>

        <form className="card" onSubmit={onSearchOwners}>
          <h2>Search Owners</h2>
          <input
            placeholder="Owner ID"
            value={ownerSearch.ownerId}
            onChange={(e) => setOwnerSearch({ ...ownerSearch, ownerId: e.target.value })}
          />
          <input
            placeholder="First name"
            value={ownerSearch.firstName}
            onChange={(e) => setOwnerSearch({ ...ownerSearch, firstName: e.target.value })}
          />
          <button type="submit">Search</button>
        </form>
      </section>

      <section className="card full">
        <h2>Animals</h2>
        <table>
          <thead>
            <tr><th>DB ID</th><th>Animal ID</th><th>Type</th><th>Owner ID</th><th>Sold</th><th>Action</th></tr>
          </thead>
          <tbody>
            {animals.map((a) => (
              <tr key={a.id}>
                <td>{a.id}</td><td>{a.animalId}</td><td>{a.type}</td><td>{a.ownerId}</td><td>{String(a.sold)}</td>
                <td><button onClick={() => api.sellAnimal(a.id).then(refresh).catch((e) => setMessage(e.message))}>Sell</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card full">
        <h2>Owner Search Results</h2>
        <table>
          <thead>
            <tr><th>ID</th><th>Username</th><th>Name</th><th>Email</th><th>Phone</th><th>Action</th></tr>
          </thead>
          <tbody>
            {ownerResults.map((o) => (
              <tr key={o.id}>
                <td>{o.id}</td>
                <td>{o.username || "Not set"}</td>
                <td>{o.firstName} {o.lastName}</td>
                <td>{o.email}</td>
                <td>{o.phoneNumber}</td>
                <td><button onClick={() => startEditOwner(o)}>Edit</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {editingOwner && (
        <section className="card full">
          <h2>Edit Owner</h2>
          <form className="grid" onSubmit={onSaveOwner}>
            <input value={editingOwner.id} readOnly />
            {editingOwner.hasCredentials ? (
              <input value={editingOwner.username} readOnly />
            ) : (
              <input
                placeholder="Set username"
                value={editingOwner.username}
                onChange={(e) => setEditingOwner({ ...editingOwner, username: e.target.value })}
                required
              />
            )}
            <input value={editingOwner.firstName} onChange={(e) => setEditingOwner({ ...editingOwner, firstName: e.target.value })} required />
            <input value={editingOwner.lastName} onChange={(e) => setEditingOwner({ ...editingOwner, lastName: e.target.value })} required />
            <input type="email" value={editingOwner.email} onChange={(e) => setEditingOwner({ ...editingOwner, email: e.target.value })} required />
            <input value={editingOwner.phoneNumber} onChange={(e) => setEditingOwner({ ...editingOwner, phoneNumber: e.target.value })} required />
            <input value={editingOwner.address} onChange={(e) => setEditingOwner({ ...editingOwner, address: e.target.value })} required />
            <input
              type="password"
              placeholder={editingOwner.hasCredentials ? "Set temporary password (optional)" : "Set temporary password"}
              value={editingOwner.temporaryPassword}
              onChange={(e) => setEditingOwner({ ...editingOwner, temporaryPassword: e.target.value })}
              required={!editingOwner.hasCredentials}
            />
            <button type="submit">Save Owner</button>
          </form>
        </section>
      )}

      <section className="card full">
        <h2>Transfer Requests</h2>
        <table>
          <thead>
            <tr><th>ID</th><th>From</th><th>To</th><th>Animals</th><th>Status</th><th>Actions</th></tr>
          </thead>
          <tbody>
            {transferRequests.map((tr) => (
              <tr key={tr.id}>
                <td>{tr.id}</td><td>{tr.fromOwnerId}</td><td>{tr.toOwnerId}</td><td>{tr.animalIds.join(", ")}</td><td>{tr.status}</td>
                <td>
                  <button onClick={() => api.approveTransferRequest(tr.id).then(refresh).catch((e) => setMessage(e.message))}>Approve</button>
                  <button onClick={() => api.rejectTransferRequest(tr.id).then(refresh).catch((e) => setMessage(e.message))}>Reject</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}

function OwnerPage({ session, onLogout }) {
  const [animals, setAnimals] = useState([]);
  const [transferForm, setTransferForm] = useState({ toOwnerId: "", animalIds: "" });
  const [requestForm, setRequestForm] = useState({ toOwnerId: "", animalIds: "", ownerEmailMessage: "" });
  const [message, setMessage] = useState("");

  const ownerId = useMemo(() => session.ownerId, [session.ownerId]);

  const refresh = async () => {
    const list = await api.getAnimals();
    setAnimals(list);
  };

  useEffect(() => {
    refresh().catch((e) => setMessage(e.message));
  }, []);

  async function onTransfer(e) {
    e.preventDefault();
    try {
      await api.transferAnimals({
        toOwnerId: Number(transferForm.toOwnerId),
        animalIds: parseIds(transferForm.animalIds)
      });
      setTransferForm({ toOwnerId: "", animalIds: "" });
      await refresh();
      setMessage("Transfer completed.");
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function onTransferRequest(e) {
    e.preventDefault();
    try {
      await api.createTransferRequest({
        fromOwnerId: ownerId,
        toOwnerId: Number(requestForm.toOwnerId),
        animalIds: parseIds(requestForm.animalIds),
        ownerEmailMessage: requestForm.ownerEmailMessage
      });
      setRequestForm({ toOwnerId: "", animalIds: "", ownerEmailMessage: "" });
      setMessage("Transfer request sent to admin.");
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function downloadOwnerReport() {
    try {
      const blob = await api.downloadReport("owner", ownerId);
      downloadBlob(blob, `owner-animal-${ownerId}.pdf`);
    } catch (err) {
      setMessage(err.message);
    }
  }

  return (
    <div className="page">
      <header className="hero topbar">
        <div>
          <h1>Owner Dashboard</h1>
          <p>{session.username} ({session.role}) - Owner ID: {ownerId}</p>
        </div>
        <button onClick={onLogout}>Logout</button>
      </header>
      {message && <div className="notice">{message}</div>}

      <section className="grid">
        <form className="card" onSubmit={onTransfer}>
          <h2>Transfer Your Animals</h2>
          <input placeholder="To Owner ID" value={transferForm.toOwnerId} onChange={(e) => setTransferForm({ ...transferForm, toOwnerId: e.target.value })} required />
          <input placeholder="Animal DB IDs (comma-separated)" value={transferForm.animalIds} onChange={(e) => setTransferForm({ ...transferForm, animalIds: e.target.value })} required />
          <button type="submit">Transfer</button>
        </form>

        <form className="card" onSubmit={onTransferRequest}>
          <h2>Email Request to Admin</h2>
          <input value={ownerId || ""} readOnly />
          <input placeholder="To Owner ID" value={requestForm.toOwnerId} onChange={(e) => setRequestForm({ ...requestForm, toOwnerId: e.target.value })} required />
          <input placeholder="Animal DB IDs (comma-separated)" value={requestForm.animalIds} onChange={(e) => setRequestForm({ ...requestForm, animalIds: e.target.value })} required />
          <textarea placeholder="Message to administrator" value={requestForm.ownerEmailMessage} onChange={(e) => setRequestForm({ ...requestForm, ownerEmailMessage: e.target.value })} required />
          <button type="submit">Send Request</button>
        </form>

        <div className="card">
          <h2>Your Report</h2>
          <button onClick={downloadOwnerReport}>Download Owner Animal Report</button>
        </div>
      </section>

      <section className="card full">
        <h2>Your Animals</h2>
        <table>
          <thead>
            <tr><th>DB ID</th><th>Animal ID</th><th>Type</th><th>Breed</th><th>Sold</th></tr>
          </thead>
          <tbody>
            {animals.map((a) => (
              <tr key={a.id}>
                <td>{a.id}</td><td>{a.animalId}</td><td>{a.type}</td><td>{a.breed}</td><td>{String(a.sold)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}

export default function App() {
  const [session, setSession] = useState(authApi.currentUser());

  async function logout() {
    await authApi.logout();
    setSession(null);
  }

  if (!session) {
    return <LoginPage onLogin={setSession} />;
  }

  if (session.mustChangePassword) {
    return <ForcePasswordResetPage session={session} onPasswordChanged={setSession} onLogout={logout} />;
  }

  if (session.role === "ADMIN") {
    return <AdminPage session={session} onLogout={logout} />;
  }

  return <OwnerPage session={session} onLogout={logout} />;
}
