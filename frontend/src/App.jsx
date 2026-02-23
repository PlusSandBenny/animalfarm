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

function AdminPage({ session, onLogout }) {
  const [ownerForm, setOwnerForm] = useState(initialOwner);
  const [animalForm, setAnimalForm] = useState(initialAnimal);
  const [transferForm, setTransferForm] = useState({ toOwnerId: "", animalIds: "" });
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
          <input placeholder="Owner ID" value={reportForm.ownerId} onChange={(e) => setReportForm({ ...reportForm, ownerId: e.target.value })} />
          <button type="button" onClick={() => onReport("ownerVsAnimal", reportForm.ownerId, `owner-vs-animal-${reportForm.ownerId}.pdf`)}>Owner vs Animal</button>
          <button type="button" onClick={() => onReport("owner", reportForm.ownerId, `owner-animal-${reportForm.ownerId}.pdf`)}>Owner Animal</button>
          <input placeholder="Parent Animal DB ID" value={reportForm.parentId} onChange={(e) => setReportForm({ ...reportForm, parentId: e.target.value })} />
          <button type="button" onClick={() => onReport("parentVsAnimal", reportForm.parentId, `parent-vs-animal-${reportForm.parentId}.pdf`)}>Parent vs Animal</button>
        </div>
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

  if (session.role === "ADMIN") {
    return <AdminPage session={session} onLogout={logout} />;
  }

  return <OwnerPage session={session} onLogout={logout} />;
}
