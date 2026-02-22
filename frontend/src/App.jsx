import { useEffect, useState } from "react";
import { api } from "./api";

const initialOwner = { firstName: "", lastName: "", email: "", phoneNumber: "", address: "" };
const initialAnimal = {
  animalId: "",
  color: "",
  dateOfBirth: "",
  breed: "",
  type: "CATTLE",
  image: "",
  parentId: "",
  ownerId: "",
  actorRole: "ADMIN"
};

export default function App() {
  const [ownerForm, setOwnerForm] = useState(initialOwner);
  const [animalForm, setAnimalForm] = useState(initialAnimal);
  const [transferForm, setTransferForm] = useState({ toOwnerId: "", animalIds: "", actorOwnerId: "", actorRole: "OWNER" });
  const [trForm, setTrForm] = useState({ fromOwnerId: "", toOwnerId: "", animalIds: "", ownerEmailMessage: "", actorRole: "OWNER" });
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

  const parseIds = (value) => value.split(",").map((x) => Number(x.trim())).filter(Boolean);

  async function onCreateOwner(e) {
    e.preventDefault();
    try {
      await api.registerOwner(ownerForm, "ADMIN");
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
        animalIds: parseIds(transferForm.animalIds),
        actorOwnerId: Number(transferForm.actorOwnerId),
        actorRole: transferForm.actorRole
      });
      setTransferForm({ toOwnerId: "", animalIds: "", actorOwnerId: "", actorRole: "OWNER" });
      await refresh();
      setMessage("Transfer completed.");
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function onCreateTransferRequest(e) {
    e.preventDefault();
    try {
      await api.createTransferRequest({
        fromOwnerId: Number(trForm.fromOwnerId),
        toOwnerId: Number(trForm.toOwnerId),
        animalIds: parseIds(trForm.animalIds),
        ownerEmailMessage: trForm.ownerEmailMessage,
        actorRole: trForm.actorRole
      });
      setTrForm({ fromOwnerId: "", toOwnerId: "", animalIds: "", ownerEmailMessage: "", actorRole: "OWNER" });
      await refresh();
      setMessage("Transfer request created.");
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function handleApprove(id) {
    try {
      await api.approveTransferRequest(id);
      await refresh();
      setMessage("Transfer request approved.");
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function handleReject(id) {
    try {
      await api.rejectTransferRequest(id);
      await refresh();
      setMessage("Transfer request rejected.");
    } catch (err) {
      setMessage(err.message);
    }
  }

  return (
    <div className="page">
      <header className="hero">
        <h1>Animal Farm Management</h1>
        <p>Manage owners, animals, transfers, sales, and PDF reports.</p>
      </header>

      {message && <div className="notice">{message}</div>}

      <section className="grid">
        <form className="card" onSubmit={onCreateOwner}>
          <h2>Register Owner (Admin)</h2>
          <input placeholder="First name" value={ownerForm.firstName} onChange={(e) => setOwnerForm({ ...ownerForm, firstName: e.target.value })} required />
          <input placeholder="Last name" value={ownerForm.lastName} onChange={(e) => setOwnerForm({ ...ownerForm, lastName: e.target.value })} required />
          <input placeholder="Email" type="email" value={ownerForm.email} onChange={(e) => setOwnerForm({ ...ownerForm, email: e.target.value })} required />
          <input placeholder="Phone" value={ownerForm.phoneNumber} onChange={(e) => setOwnerForm({ ...ownerForm, phoneNumber: e.target.value })} required />
          <input placeholder="Address" value={ownerForm.address} onChange={(e) => setOwnerForm({ ...ownerForm, address: e.target.value })} required />
          <button type="submit">Create Owner</button>
        </form>

        <form className="card" onSubmit={onCreateAnimal}>
          <h2>Register Animal (Admin)</h2>
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
          <select value={transferForm.actorRole} onChange={(e) => setTransferForm({ ...transferForm, actorRole: e.target.value })}>
            <option value="OWNER">OWNER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
          <input placeholder="Actor Owner ID" value={transferForm.actorOwnerId} onChange={(e) => setTransferForm({ ...transferForm, actorOwnerId: e.target.value })} required />
          <input placeholder="To Owner ID" value={transferForm.toOwnerId} onChange={(e) => setTransferForm({ ...transferForm, toOwnerId: e.target.value })} required />
          <input placeholder="Animal DB IDs (comma-separated)" value={transferForm.animalIds} onChange={(e) => setTransferForm({ ...transferForm, animalIds: e.target.value })} required />
          <button type="submit">Transfer</button>
        </form>

        <form className="card" onSubmit={onCreateTransferRequest}>
          <h2>Owner Email Transfer Request</h2>
          <input placeholder="From Owner ID" value={trForm.fromOwnerId} onChange={(e) => setTrForm({ ...trForm, fromOwnerId: e.target.value })} required />
          <input placeholder="To Owner ID" value={trForm.toOwnerId} onChange={(e) => setTrForm({ ...trForm, toOwnerId: e.target.value })} required />
          <input placeholder="Animal DB IDs (comma-separated)" value={trForm.animalIds} onChange={(e) => setTrForm({ ...trForm, animalIds: e.target.value })} required />
          <textarea placeholder="Owner email message to admin" value={trForm.ownerEmailMessage} onChange={(e) => setTrForm({ ...trForm, ownerEmailMessage: e.target.value })} required />
          <button type="submit">Create Request</button>
        </form>
      </section>

      <section className="card full">
        <h2>Animals</h2>
        <table>
          <thead>
            <tr>
              <th>DB ID</th>
              <th>Animal ID</th>
              <th>Type</th>
              <th>Owner ID</th>
              <th>Sold</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {animals.map((a) => (
              <tr key={a.id}>
                <td>{a.id}</td>
                <td>{a.animalId}</td>
                <td>{a.type}</td>
                <td>{a.ownerId}</td>
                <td>{String(a.sold)}</td>
                <td><button onClick={() => api.sellAnimal(a.id).then(refresh).catch((e) => setMessage(e.message))}>Sell (Admin)</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card full">
        <h2>Transfer Requests</h2>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>From</th>
              <th>To</th>
              <th>Animals</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {transferRequests.map((tr) => (
              <tr key={tr.id}>
                <td>{tr.id}</td>
                <td>{tr.fromOwnerId}</td>
                <td>{tr.toOwnerId}</td>
                <td>{tr.animalIds.join(", ")}</td>
                <td>{tr.status}</td>
                <td>
                  <button onClick={() => handleApprove(tr.id)}>Approve</button>
                  <button onClick={() => handleReject(tr.id)}>Reject</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card full">
        <h2>Reports (PDF)</h2>
        <div className="report-row">
          <input placeholder="Owner ID" value={reportForm.ownerId} onChange={(e) => setReportForm({ ...reportForm, ownerId: e.target.value })} />
          <a href={api.reportUrl("ownerVsAnimal", reportForm.ownerId)} target="_blank" rel="noreferrer">Owner vs Animal</a>
          <a href={api.reportUrl("owner", reportForm.ownerId)} target="_blank" rel="noreferrer">Owner Animal Report</a>
        </div>
        <div className="report-row">
          <input placeholder="Parent Animal DB ID" value={reportForm.parentId} onChange={(e) => setReportForm({ ...reportForm, parentId: e.target.value })} />
          <a href={api.reportUrl("parentVsAnimal", reportForm.parentId)} target="_blank" rel="noreferrer">Parent vs Animal</a>
        </div>
      </section>
    </div>
  );
}
